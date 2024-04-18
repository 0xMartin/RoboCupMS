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
import com.robogames.RoboCupMS.Entity.Competition;
import com.robogames.RoboCupMS.Entity.Discipline;
import com.robogames.RoboCupMS.Entity.MatchGroup;
import com.robogames.RoboCupMS.Entity.MatchState;
import com.robogames.RoboCupMS.Entity.Playground;
import com.robogames.RoboCupMS.Entity.Robot;
import com.robogames.RoboCupMS.Entity.RobotMatch;
import com.robogames.RoboCupMS.Module.OrderManagement.Bussiness.Object.MatchQueue;
import com.robogames.RoboCupMS.Module.OrderManagement.Bussiness.Object.MultiMatchGroupObj;
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
                // refresh jen v pripade provedeni zmen v zapasech
                if (sender instanceof MatchService) {
                    if (data instanceof MatchService.Message) {
                        MatchService.Message msg = (MatchService.Message) data;
                        if (msg.equals(MatchService.Message.WRITE_SCORE)
                                || msg.equals(MatchService.Message.REMATCH)
                                || msg.equals(MatchService.Message.REMOVE)
                                || msg.equals(MatchService.Message.REMOVE_ALL)
                                || msg.equals(MatchService.Message.WRITE_SCORE)) {
                            // refresh systemu pro rizeni poradi
                            refreshSystem();
                        }
                    }
                }
            }
        });
    }

    /**
     * Spusti modul pro rizeni poradi. Modul bude mozne spustit jen pro soutez,
     * ktery byla jiz zahajna.
     * 
     * @param year Rocnik souteze
     * @throws Exception
     */
    public void run(int year) throws Exception {
        Optional<Competition> competition = this.competitionRepository.findByYear(year);
        if (!competition.isPresent()) {
            throw new Exception(String.format("failure, compotition [%d] not exists", year));
        }

        if (!competition.get().getStarted()) {
            throw new Exception(String.format("failure, compotition [%d] has not started yet", year));
        }

        // nastavi rocnik souteze
        OrderManagementService.YEAR = year;

        // refresh systemu
        this.refreshSystem();
    }

    /**
     * Navrati informaci o tom zda je servis spusten
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
                // prida zapas
                matches.add(first);

                // pokud jde o skupinovy zapas tak prida i ostatni zapasy skupiny
                MatchGroup matchGroup = first.getMatchGroup();
                if (matchGroup != null) {
                    matches.addAll(matchGroup.getMatches());
                }
            }
        });

        return matches;
    }

    /**
     * Vyzada zmenu poradi zapasu ve fronte
     * 
     * @param id ID zápasu, o kterém rozhodčí rozhodne, aby byl odehrán v dánou
     *           chvíli. Zápas s tímto ID bude přesunut na první místo ve frontě.
     *           (pokud bude zadána záporná neplatná hodnota pak systém vybere
     *           náhodně ze seznamu čekajících zápasů)
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
     * @param id ID robota
     * @return Seznam vsech zapasu robota, ktere jeste cekaji na odehrani
     */
    public List<RobotMatch> upcommingMatches(long id) throws Exception {
        // overi zda robot existuje
        Optional<Robot> robot = this.robotRepository.findById(id);
        if (!robot.isPresent()) {
            throw new Exception(String.format("failure, robot with ID [%d] not exists", id));
        }

        // seznam vsech zapasu robota, ktere prace cekaji na odehrani "stav:
        // EMatchState.WAITING nebo EMatchState.REMATCH"
        Stream<RobotMatch> matches = robot.get().getMatches().stream()
                .filter((m) -> (m.getState().getName() == EMatchState.WAITING
                        || m.getState().getName() == EMatchState.REMATCH));

        return matches.collect(Collectors.toList());
    }

    /**
     * Vygeneruje skupinove zapasy "kazdy s kazdym" (sumo, robo strong, ...).
     * Neoveruje zda jde o disciplinu, ktera umoznuje zapaseni robot proti robotu
     * 
     * 
     * @param multiMatchGroupObj Objekt definujici parametry pro vygenerovani vsech
     *                           zapasu
     * @return Navrati identifikacni cislo tvurce zapasovych skupin (nasledne muze
     *         byt uplatneno pro odstraneni zapasu)
     */
    public long generateMatches(MultiMatchGroupObj multiMatchGroupObj) throws Exception {
        // overi zda roucnik souteze existuje
        if (!this.competitionRepository.findByYear(multiMatchGroupObj.getYear()).isPresent()) {
            throw new Exception(String.format("failure, compatition [%d] not exists", multiMatchGroupObj.getYear()));
        }

        // overi zda hriste existuje
        Optional<Playground> playground = this.playgroundRepository.findById(multiMatchGroupObj.getPlaygroundID());
        if (!playground.isPresent()) {
            throw new Exception(
                    String.format("failure, playground with ID [%d] not exists", multiMatchGroupObj.getPlaygroundID()));
        }

        // ID kazdeho robota overi zda (existuje, je registrovany v danem rocniku
        // souteze a zda jsou vsichni roboti ze stejne kategorie a discipliny)
        boolean first = true;
        ECategory mainCategory = ECategory.LOW_AGE_CATEGORY;
        Discipline mainDiscipline = null;
        for (Long id : multiMatchGroupObj.getRobots()) {
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
            if (robot.get().getTeamRegistration().getCompatitionYear() != multiMatchGroupObj.getYear()) {
                throw new Exception(String.format("failure, registration of robot with ID [%d] is not confirmed", id));
            }
            // prvotni inicializace kategorie a discipliny vsech robotu
            if (first) {
                mainCategory = robot.get().getCategory();
                mainDiscipline = robot.get().getDiscipline();
                first = false;
            }
            // overeni stejne kategorie a discipliny
            if (robot.get().getCategory() != mainCategory) {
                throw new Exception(String.format("failure, robot with ID [%d] is from different category", id));
            }
            if (robot.get().getDiscipline().getID() != mainDiscipline.getID()) {
                throw new Exception(String.format("failure, robot with ID [%d] is from different discipline", id));
            }
        }

        // vygeneruje unikatni id pro naslednou moznost odstraneni vsech techto
        // vygenerovanych zapasu
        long creatorIdentifier = MatchGroup.generateCreatorIdentifier(this.matchGroupRepository);

        // vygeneruje vsechny kombinace zapasu mezi roboty
        MatchState matchState = this.matchStateRepository.findByName(EMatchState.WAITING).get();
        Long[] robots = multiMatchGroupObj.getRobots();
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

        // refresh systemu pro rizeni poradi
        this.refreshSystem();

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

        logger.info("OrderManagementService refresh");

        // sychrnonizace
        OrderManagementService.MATCH_GUEUES.forEach((p, queue) -> {
            // synchronizace dat
            queue.synchronize(this.robotMatchRepository);
            // odstraneni odehranych zapasu
            int cnt = queue.removeAllDone();
            logger.info(String.format("[Playground ID: %d] removed from queue: %d", p, cnt));
        });

        // prida vsechny zapasy, ktere cekaji na odehrani
        Stream<RobotMatch> matches = this.robotMatchRepository.findAll().stream()
                .filter((m) -> (m.getRobot().getTeamRegistration().getCompatitionYear() == YEAR));
        matches.forEach((m) -> {
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
