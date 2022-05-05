package com.robogames.RoboCupMS.Module.OrderManagement.Bussiness.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.robogames.RoboCupMS.Communication;
import com.robogames.RoboCupMS.Business.Enum.ECategory;
import com.robogames.RoboCupMS.Business.Enum.EMatchState;
import com.robogames.RoboCupMS.Business.Service.MatchService;
import com.robogames.RoboCupMS.Communication.CallBack;
import com.robogames.RoboCupMS.Entity.Discipline;
import com.robogames.RoboCupMS.Entity.MatchGroup;
import com.robogames.RoboCupMS.Entity.MatchState;
import com.robogames.RoboCupMS.Entity.Playground;
import com.robogames.RoboCupMS.Entity.Robot;
import com.robogames.RoboCupMS.Entity.RobotMatch;
import com.robogames.RoboCupMS.Module.OrderManagement.Bussiness.Model.MatchQueue;
import com.robogames.RoboCupMS.Repository.CompetitionRepository;
import com.robogames.RoboCupMS.Repository.MatchGroupRepository;
import com.robogames.RoboCupMS.Repository.MatchStateRepository;
import com.robogames.RoboCupMS.Repository.PlaygroundRepository;
import com.robogames.RoboCupMS.Repository.RobotMatchRepository;
import com.robogames.RoboCupMS.Repository.RobotRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderManagementService {

    private static final Logger logger = LoggerFactory.getLogger(OrderManagementService.class);

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private RobotMatchRepository robotMatchRepository;

    @Autowired
    private MatchGroupRepository matchGroupRepository;

    @Autowired
    private RobotRepository robotRepository;

    @Autowired
    private PlaygroundRepository playgroundRepository;

    @Autowired
    private MatchStateRepository matchStateRepository;

    /**
     * Fronty zapasu cekajicich na odehrani pro kazdou disciplinu
     */
    private final static Map<Long, MatchQueue> MATCH_GUEUES = Collections
            .synchronizedMap(new HashMap<Long, MatchQueue>());

    /**
     * Rocnik souteze
     */
    private static int YEAR = -1;

    public OrderManagementService() {
        // bude naslouchat komunikacnimu systemu aplikace
        Communication.getInstance().getCallBacks().add(new CallBack() {
            @Override
            public void callBack(Object sender, Object data) {
                // pokud zachyti http request na endpoint "GlobalConfig.AUTH_PREFIX +
                // /match/writeScore" -> zapas byl
                // odehran -> dojde k obnoveni
                if (sender instanceof MatchService) {
                    if (data instanceof String) {
                        if (((String) data).equals("writeScore")) {
                            // refresh systemu pro rizeni poradi
                            refreshSystem();
                        }
                    }
                }
            }
        });
    }

    /**
     * Spusti servis pro rizeni poradi
     * 
     * @param year Rocnik souteze
     * @throws Exception
     */
    public void run(int year) throws Exception {
        if (!this.competitionRepository.findByYear(year).isPresent()) {
            throw new Exception(String.format("failure, compotition [%d] not exists", year));
        }

        // nastavi rocnik souteze
        OrderManagementService.YEAR = year;

        // refresh systemu
        this.refreshSystem();
    }

    /**
     * Stav systemu
     * 
     * @return Stav
     */
    public boolean isRunning() {
        return OrderManagementService.YEAR != -1;
    }

    /**
     * Vyzada refresh systemu, pokud dojde k zamrznuti
     * 
     * @throws Exception
     */
    public void requestRefresh() throws Exception {
        if (OrderManagementService.YEAR == -1) {
            throw new Exception("Order Management Service is not running!");
        }

        OrderManagementService.MATCH_GUEUES.clear();

        this.refreshSystem();
    }

    /**
     * Navrati seznam vsech zapasu, ktere maji byt nyni odehrany na prislusnych
     * hristich
     * 
     * @return Vsechny zapasy, ktere maji byt nyni odehrany na prislusnych hristich
     */
    public List<RobotMatch> currentMatches() throws Exception {
        if (OrderManagementService.YEAR == -1) {
            throw new Exception("failure, order Management Service is not running!");
        }

        List<RobotMatch> matches = new ArrayList<RobotMatch>();

        OrderManagementService.MATCH_GUEUES.forEach((p, queue) -> {
            RobotMatch first = queue.getFirst();
            if (first != null) {
                matches.add(first);
            }
        });

        return matches;
    }

    /**
     * Vyzada zmenu zapasu, ktery ma byt aktulane odehran na nekterem z hrist dane
     * discipliny
     * 4
     * 
     * @param id ID dalsiho zapasu, ktery rozhodci chce, aby byl odehran
     *           (pokud
     *           bude zadana zaporna neplatna hodnota pak system vybere
     *           nahodne ze
     *           seznamu cekajicih zapasu)
     * @return
     */
    public void requestAnotherMatch(long id) throws Exception {
        if (OrderManagementService.YEAR == -1) {
            throw new Exception("failure, order Management Service is not running!");
        }

        Optional<RobotMatch> match = this.robotMatchRepository.findById(id);
        if (!match.isPresent()) {
            throw new Exception(String.format("failure, match with ID [%d] not exists", id));
        }

        MatchQueue matchQueue = OrderManagementService.MATCH_GUEUES.get(match.get().getPlayground().getID());

        if (matchQueue == null) {
            throw new Exception("match queue not exists");
        }

        // zapas presune na prvni misto ve fronte
        matchQueue.setFirst(id);
    }

    /**
     * Navrati pro robota seznam vsech nadchazejicich zapasu
     * 
     * @param year Rocnik souteze
     * @param id   ID robota
     * @return Seznam vsech zapasu robota, ktere jeste cekaji na odehrani
     */
    public List<RobotMatch> upcommingMatches(long id) throws Exception {
        // overi zda robot existuje
        Optional<Robot> robot = this.robotRepository.findById(id);
        if (!robot.isPresent()) {
            throw new Exception(String.format("failure, robot with ID [%d] not exists", id));
        }

        // seznam vsech zapasu robota, ktere prace cekaji na odehrani "stav:
        // EMatchState.WAITING"
        Stream<RobotMatch> matches = robot.get().getMatches().stream()
                .filter((m) -> (m.getState().getName() == EMatchState.WAITING));

        return matches.collect(Collectors.toList());
    }

    /**
     * Vygeneruje skupinove zapasy "kazdy s kazdym" (sumo, robo strong, ...).
     * Neoveruje zda jde o disciplinu, ktera umoznuje zapaseni robot proti robotu
     * 
     * 
     * @param year         Rocnik souteze
     * @param robots       Seznam ID robotu, pro ktere se zapasy vytvori
     * @param playgroundID ID hriste kde se zapasy budou konat
     * @return Navrati identifikacni cislo tvurce zapasovych skupin (nasledne muze
     *         byt uplatneno pro odstraneni zapasu)
     */
    public long generateMatches(int year, Long[] robots, long playgroundID) throws Exception {
        // overi zda roucnik souteze existuje
        if (!this.competitionRepository.findByYear(year).isPresent()) {
            throw new Exception(String.format("failure, compatition [%d] not exists", year));
        }

        // overi zda hriste existuje
        Optional<Playground> playground = this.playgroundRepository.findById(playgroundID);
        if (!playground.isPresent()) {
            throw new Exception(String.format("failure, playground with ID [%d] not exists", playgroundID));
        }

        // ID kazdeho robota overi zda (existuje, je registrovany v danem rocniku
        // souteze a zda jsou vsichni roboti ze stejne kategorie a discipliny)
        boolean first = true;
        ECategory mainCategory = ECategory.OPEN;
        Discipline mainDiscipline = null;
        for (Long id : robots) {
            // overeni existence
            Optional<Robot> robot = this.robotRepository.findById(id);
            if (!robot.isPresent()) {
                throw new Exception(String.format("failure, robot with ID [%d] not exists", id));
            }
            // overeni potvrezeni registrace
            if (!robot.get().getConfirmed()) {
                throw new Exception(String.format("failure, registration of robot with ID [%d] is not confirmed", id));
            }
            // overeni zda je robot registrovan v danem rocniku souteze
            if (robot.get().getTeamRegistration().getCompatitionYear() != year) {
                throw new Exception(String.format("failure, registration of robot with ID [%d] is not confirmed", id));
            }
            // prvotni inicializace kategorie a discipliny vsech robotu
            if (first) {
                mainCategory = robot.get().getCategory();
                mainDiscipline = robot.get().getDiscipline();
            }
            // overeni stejne kategorie a discipliny
            if (robot.get().getCategory() != mainCategory) {
                throw new Exception(String.format("failure, robot with ID [%d] is from different category", id));
            }
            if (robot.get().getDiscipline() != mainDiscipline) {
                throw new Exception(String.format("failure, robot with ID [%d] is from different discipline", id));
            }
        }

        // vygeneruje unikatni id pro naslednou moznost odstraneni vsech techto
        // vygenerovanych zapasu
        long creatorIdentifier = MatchGroup.generateCreatorIdentifier(this.matchGroupRepository);

        // vygeneruje vsechny kombinace zapasu mezi roboty
        MatchState matchState = this.matchStateRepository.findByName(EMatchState.WAITING).get();
        for (int i = 0; i < robots.length - 1; ++i) {
            for (int j = i + 1; j < robots.length; ++j) {
                // vytvori zapasovou skupinu
                MatchGroup group = new MatchGroup(creatorIdentifier);
                this.matchGroupRepository.save(group);

                Robot r1 = this.robotRepository.getById(robots[i]);
                Robot r2 = this.robotRepository.getById(robots[j]);
                // vytvori zapas pro oba roboty
                RobotMatch m1 = new RobotMatch(r1, group, playground.get(), matchState);
                RobotMatch m2 = new RobotMatch(r2, group, playground.get(), matchState);
                this.robotMatchRepository.save(m1);
                this.robotMatchRepository.save(m2);
            }
        }

        return creatorIdentifier;
    }

    /**
     * 10
     * Refresh systemu pro rizeni poradi (vola se automaticky pri kazdem http
     * reqestu na zapis skore nejakeho zapasu)
     */
    private synchronized void refreshSystem() {
        // this.nextMatches
        if (OrderManagementService.YEAR == -1) {
            logger.error("Order Management Service is not running!");
            return;
        }

        // seznam zapasu cekajicich na odehrani
        Stream<RobotMatch> waiting = this.robotMatchRepository.findAll().stream()
                .filter((m) -> (m.getRobot().getTeamRegistration().getCompatitionYear() == YEAR &&
                        (m.getState().getName() == EMatchState.WAITING
                                || m.getState().getName() == EMatchState.REMATCH)));

        logger.info("OrderManagementService refresh");

        // sychrnonizace a odstrani odehranych zapasu
        OrderManagementService.MATCH_GUEUES.forEach((p, queue) -> {
            // synchronizace
            queue.synchronize(this.robotMatchRepository);
            // odstraneni
            int cnt = queue.removeAllDone();
            logger.info(String.format("[Playground ID: %d] removed from queue: %d", p, cnt));
        });

        // prida vsechny zapasy, ktere cekaji na odehrani
        waiting.forEach((m) -> {
            MatchQueue queue = OrderManagementService.MATCH_GUEUES.get(m.getPlayground().getID());
            if (queue == null) {
                queue = new MatchQueue(m.getPlayground());
                OrderManagementService.MATCH_GUEUES.put(m.getPlayground().getID(), queue);
            }
            queue.add(m);
            logger.info(String.format("[Playground ID: %d] added new match with ID [%d]", m.getPlayground().getID(),
                    m.getID()));
        });
    }

}