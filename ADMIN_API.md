# Admin API

Curl examples for Admin API endpoints. Only for localhost testing.

**Base URL:** `http://localhost:8080/api/admin`  

## Get endpoints

```bash
# ziskani vsech tymu
curl -X GET "http://localhost:8080/api/team/all" \
  -H "Authorization: Bearer YOUR_TOKEN"

# ziskani vsech registraci tymu do soutezi
curl -X GET "http://localhost:8080/api/teamRegistration/all" \
  -H "Authorization: Bearer YOUR_TOKEN"

# ziskani vsech uzivatelu
curl -X GET "http://localhost:8080/api/user/all" \
  -H "Authorization: Bearer YOUR_TOKEN"

# ziskani vsech robotu
curl -X GET "http://localhost:8080/api/robot/all?year=2026" \
  -H "Authorization: Bearer YOUR_TOKEN"

# ziskani vsech soutezi
curl -X GET "http://localhost:8080/api/competition/all"

# ziskani vsech disciplin
curl -X GET "http://localhost:8080/api/discipline/all"
```

## Team _/api/admin/team_

```bash
# Vytvorit tym
curl -X POST "http://localhost:8080/api/admin/team/create" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "TestTeam", "leaderId": 1003, "memberIds": [1003, 1004]}'

# Editovat tym
curl -X PUT "http://localhost:8080/api/admin/team/edit?id=2001" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "TestTeam", "leaderId": 1004}'

# Pridat uzivatele do tymu
curl -X PUT "http://localhost:8080/api/admin/team/addUser?teamId=2001&userId=1004" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Odebrat uzivatele z tymu
curl -X PUT "http://localhost:8080/api/admin/team/removeUser?teamId=2001&userId=1004" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Nastavit vedouciho
curl -X PUT "http://localhost:8080/api/admin/team/setLeader?teamId=2001&newLeaderId=1004" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Presunout uzivatele do jineho tymu
curl -X PUT "http://localhost:8080/api/admin/team/transferUser?userId=1004&newTeamId=2001" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Smazat tym
curl -X DELETE "http://localhost:8080/api/admin/team/remove?id=2001" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Registrace tymu _/api/admin/registration_

```bash
# Registrovat tym do souteze
curl -X POST "http://localhost:8080/api/admin/registration/create" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"teamId": 2002, "year": 2026, "teacherName": "Jan", "teacherSurname": "Novak", "teacherContact": "jan@test.cz"}'

# Editovat ucitele
curl -X PUT "http://localhost:8080/api/admin/registration/editTeacher?id=2004" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"teacherName": "Petr", "teacherSurname": "Svoboda", "teacherContact": "petr@test.cz"}'

# Zmenit kategorii (LOW_AGE_CATEGORY / HIGH_AGE_CATEGORY)
curl -X PUT "http://localhost:8080/api/admin/registration/forceChangeCategory?id=2004&category=LOW_AGE_CATEGORY" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Smazat registraci
curl -X DELETE "http://localhost:8080/api/admin/registration/remove?id=2004" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Robot _/api/admin/robot_

```bash
# Vytvorit robota
curl -X POST "http://localhost:8080/api/admin/robot/create" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"teamRegistrationId": 2005, "name": "TestRobot", "disciplineId": 1}'

# Editovat robota (disciplineId: -1 = zrusit disciplinu)
curl -X PUT "http://localhost:8080/api/admin/robot/edit?id=1" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "NovyRobot", "number": 42, "disciplineId": 2, "confirmed": false}'

# Potvrdit/zrusit potvrzeni robota
curl -X PUT "http://localhost:8080/api/admin/robot/forceConfirm?id=1&confirmed=true" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Smazat robota
curl -X DELETE "http://localhost:8080/api/admin/robot/remove?id=1" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Force smazat robota (i potvrzeneho)
curl -X DELETE "http://localhost:8080/api/admin/robot/forceRemove?id=1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```
