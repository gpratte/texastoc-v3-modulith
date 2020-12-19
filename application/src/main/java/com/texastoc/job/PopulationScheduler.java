package com.texastoc.job;

import com.texastoc.controller.request.CreateGamePlayerRequest;
import com.texastoc.controller.request.UpdateGamePlayerRequest;
import com.texastoc.model.game.FirstTimeGamePlayer;
import com.texastoc.model.game.Game;
import com.texastoc.model.game.GamePlayer;
import com.texastoc.model.season.Season;
import com.texastoc.model.user.Player;
import com.texastoc.service.GameService;
import com.texastoc.service.PlayerService;
import com.texastoc.service.SeasonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * When running with an embedded H2 database populate the current season with games.
 */
@Profile("!mysql")
@Slf4j
@Component
public class PopulationScheduler {

  private final SeasonService seasonService;
  private final GameService gameService;
  private final PlayerService playerService;

  private final Random random = new Random(System.currentTimeMillis());

  public PopulationScheduler(SeasonService seasonService, GameService gameService, PlayerService playerService) {
    this.seasonService = seasonService;
    this.gameService = gameService;
    this.playerService = playerService;
  }

  // delay one minute then run every hour
  @Scheduled(fixedDelay = 3600000, initialDelay = 60000)
  public void populate() {
    createSeason();
  }

  private void createSeason() {
    LocalDate now = LocalDate.now();

    try {
      List<Season> seasons = seasonService.getSeasons();
      if (seasons.size() > 0) {
        return;
      }
      log.info("Populating");
      int year;
      switch (now.getMonth()) {
        case JANUARY:
        case FEBRUARY:
        case MARCH:
        case APRIL:
          // if before May then create for the previous year
          year = now.getYear() - 1;
          break;
        default:
          year = now.getYear();
      }
      Season season = seasonService.createSeason(year);
      createGames(season);
      log.info("Done populating");
    } catch (Exception e) {
      log.error("Problem populating", e);
    }
  }

  private void createGames(Season season) {
    LocalDate now = LocalDate.now();

    LocalDate seasonStart = season.getStart();
    LocalDate gameDate = findNextThursday(seasonStart);

    while (!gameDate.isAfter(now)) {
      // pick one of the first players to be the host
      List<Player> players = playerService.get();
      int numPlayers = players.size();
      Player player = null;
      if (numPlayers > 5) {
        player = players.get(random.nextInt(5));
      } else {
        player = players.get(random.nextInt(numPlayers));
      }

      Game game = gameService.createGame(Game.builder()
        .hostId(player.getId())
        .date(gameDate)
        .transportRequired(false)
        .build());

      addGamePlayers(game.getId());
      addGamePlayersRebuy(game.getId());
      addGamePlayersFinish(game.getId());

      // Is this the last game? Check if the next game is after now.
      LocalDate nextGameDate = findNextThursday(gameDate.plusDays(1));
      if (!nextGameDate.isAfter(now)) {
        // finalize the game
        gameService.endGame(game.getId());
      }
      gameDate = findNextThursday(gameDate.plusDays(1));
    }
  }

  private void addGamePlayers(int gameId) {
    Game game = gameService.getGame(gameId);

    int numPlayersToAddToGame = game.getDate().getDayOfMonth();
    if (numPlayersToAddToGame < 2) {
      numPlayersToAddToGame = 2;
    }

    List<Player> existingPlayers = playerService.get();

    if (existingPlayers.size() < 30) {
      addNewPlayer(game);
      numPlayersToAddToGame -= 1;
    }

    if (existingPlayers.size() >= numPlayersToAddToGame) {
      // use the existing players
      List<Integer> existingPlayersIdsInGame = new ArrayList<>(existingPlayers.size());

      // Grab an existing player if not already added to game
      while (numPlayersToAddToGame > 0) {
        Player existingPlayer = existingPlayers.get(random.nextInt(existingPlayers.size()));
        if (existingPlayersIdsInGame.contains(existingPlayer.getId())) {
          continue;
        }
        // Add existing player to the game
        addExistingPlayer(game, existingPlayer);
        existingPlayersIdsInGame.add(existingPlayer.getId());
        --numPlayersToAddToGame;
      }
    } else {
      // not enough existing players so use all existing players and then add new players
      for (Player existingPlayer : existingPlayers) {
        addExistingPlayer(game, existingPlayer);
        --numPlayersToAddToGame;
      }

      // now add new players
      for (int i = 0; i < numPlayersToAddToGame; i++) {
        addNewPlayer(game);
      }
    }
  }

  private void addGamePlayersRebuy(int gameId) {
    Game game = gameService.getGame(gameId);

    List<GamePlayer> gamePlayers = game.getPlayers();
    for (GamePlayer gamePlayer : gamePlayers) {
      if (random.nextBoolean()) {
        UpdateGamePlayerRequest ugpr = new UpdateGamePlayerRequest();
        ugpr.setBuyInCollected(true);
        ugpr.setRebuyAddOnCollected(true);
        Integer annualTocCollect = gamePlayer.getAnnualTocCollected();
        if (annualTocCollect != null && annualTocCollect > 0) {
          ugpr.setAnnualTocCollected(true);
        }
        Integer qAnnualTocCollect = gamePlayer.getQuarterlyTocCollected();
        if (qAnnualTocCollect != null && qAnnualTocCollect > 0) {
          ugpr.setQuarterlyTocCollected(true);
        }
        gameService.updateGamePlayer(game.getId(), gamePlayer.getId(), ugpr);
      }
    }
  }

  private void addGamePlayersFinish(int gameId) {
    Game game = gameService.getGame(gameId);

    List<GamePlayer> gamePlayers = game.getPlayers();

    for (int place = 1; place <= 10 && gamePlayers.size() > 0; ++place) {
      GamePlayer gamePlayer = gamePlayers.remove(random.nextInt(gamePlayers.size()));
      UpdateGamePlayerRequest ugpr = new UpdateGamePlayerRequest();
      ugpr.setBuyInCollected(true);
      Integer rebuy = gamePlayer.getRebuyAddOnCollected();
      if (rebuy != null && rebuy > 0) {
        ugpr.setRebuyAddOnCollected(true);
      }
      Integer annualTocCollect = gamePlayer.getAnnualTocCollected();
      if (annualTocCollect != null && annualTocCollect > 0) {
        ugpr.setAnnualTocCollected(true);
      }
      Integer qAnnualTocCollect = gamePlayer.getQuarterlyTocCollected();
      if (qAnnualTocCollect != null && qAnnualTocCollect > 0) {
        ugpr.setQuarterlyTocCollected(true);
      }
      ugpr.setPlace(place);
      gameService.updateGamePlayer(game.getId(), gamePlayer.getId(), ugpr);
    }
  }

  private void addExistingPlayer(Game game, Player existingPlayer) {
    CreateGamePlayerRequest cgpr = new CreateGamePlayerRequest();
    cgpr.setPlayerId(existingPlayer.getId());
    cgpr.setBuyInCollected(true);
    cgpr.setAnnualTocCollected(random.nextBoolean());
    // 20% in the quarterly
    if (random.nextInt(5) == 0) {
      cgpr.setQuarterlyTocCollected(random.nextBoolean());
    }
    gameService.createGamePlayer(game.getId(), cgpr);
  }

  private void addNewPlayer(Game game) {
    FirstTimeGamePlayer ftgp = new FirstTimeGamePlayer();
    int firstNameIndex = random.nextInt(300);
    ftgp.setFirstName(firstNames[firstNameIndex]);
    int lastNameIndex = random.nextInt(300);
    ftgp.setLastName(lastNames[lastNameIndex]);
    ftgp.setBuyInCollected(true);
    ftgp.setAnnualTocCollected(random.nextBoolean());
    ftgp.setQuarterlyTocCollected(random.nextBoolean());
    gameService.createFirstTimeGamePlayer(game.getId(), ftgp);
  }

  private LocalDate findNextThursday(LocalDate date) {
    while (date.getDayOfWeek() != DayOfWeek.THURSDAY) {
      date = date.plusDays(1);
    }
    return date;
  }

  static final String[] firstNames = {"James", "John", "Robert", "Michael", "Mary", "William", "David", "Joseph", "Richard", "Charles", "Thomas", "Christopher", "Daniel", "Elizabeth", "Matthew", "Patricia", "George", "Jennifer", "Linda", "Anthony", "Barbara", "Donald", "Paul", "Mark", "Andrew", "Edward", "Steven", "Kenneth", "Margaret", "Joshua", "Kevin", "Brian", "Susan", "Dorothy", "Ronald", "Sarah", "Timothy", "Jessica", "Jason", "Helen", "Nancy", "Betty", "Karen", "Jeffrey", "Lisa", "Ryan", "Jacob", "Frank", "Gary", "Nicholas", "Anna", "Eric", "Sandra", "Stephen", "Emily", "Ashley", "Jonathan", "Kimberly", "Donna", "Ruth", "Carol", "Michelle", "Larry", "Laura", "Amanda", "Justin", "Raymond", "Scott", "Samuel", "Brandon", "Melissa", "Benjamin", "Rebecca", "Deborah", "Stephanie", "Sharon", "Kathleen", "Cynthia", "Gregory", "Jack", "Amy", "Henry", "Shirley", "Patrick", "Alexander", "Emma", "Angela", "Catherine", "Virginia", "Katherine", "Walter", "Dennis", "Jerry", "Brenda", "Pamela", "Frances", "Tyler", "Nicole", "Christine", "Aaron", "Peter", "Samantha", "Evelyn", "Jose", "Rachel", "Alice", "Douglas", "Janet", "Carolyn", "Adam", "Debra", "Harold", "Nathan", "Martha", "Maria", "Marie", "Zachary", "Arthur", "Heather", "Diane", "Julie", "Joyce", "Carl", "Grace", "Victoria", "Albert", "Rose", "Joan", "Kyle", "Christina", "Kelly", "Ann", "Lauren", "Doris", "Julia", "Jean", "Lawrence", "Judith", "Olivia", "Kathryn", "Joe", "Mildred", "Willie", "Gerald", "Lillian", "Roger", "Cheryl", "Megan", "Jeremy", "Keith", "Hannah", "Andrea", "Ethan", "Sara", "Terry", "Jacqueline", "Christian", "Harry", "Jesse", "Sean", "Teresa", "Ralph", "Austin", "Gloria", "Janice", "Roy", "Theresa", "Louis", "Noah", "Bruce", "Billy", "Judy", "Bryan", "Madison", "Eugene", "Beverly", "Jordan", "Denise", "Jane", "Marilyn", "Amber", "Dylan", "Danielle", "Abigail", "Charlotte", "Diana", "Brittany", "Russell", "Natalie", "Wayne", "Irene", "Ruby", "Annie", "Sophia", "Alan", "Juan", "Gabriel", "Howard", "Fred", "Vincent", "Lori", "Philip", "Kayla", "Alexis", "Tiffany", "Florence", "Isabella", "Kathy", "Louise", "Logan", "Lois", "Tammy", "Crystal", "Randy", "Bonnie", "Phyllis", "Anne", "Taylor", "Victor", "Bobby", "Erin", "Johnny", "Phillip", "Martin", "Josephine", "Alyssa", "Bradley", "Ella", "Shawn", "Clarence", "Travis", "Ernest", "Stanley", "Allison", "Craig", "Shannon", "Elijah", "Edna", "Peggy", "Tina", "Leonard", "Robin", "Dawn", "Carlos", "Earl", "Eleanor", "Jimmy", "Francis", "Cody", "Caleb", "Mason", "Rita", "Danny", "Isaac", "Audrey", "Todd", "Wanda", "Clara", "Ethel", "Paula", "Cameron", "Norma", "Dale", "Ellen", "Luis", "Alex", "Marjorie", "Luke", "Jamie", "Nathaniel", "Allen", "Leslie", "Joel", "Evan", "Edith", "Connie", "Eva", "Gladys", "Carrie", "Ava", "Frederick", "Wendy", "Hazel", "Valerie", "Curtis", "Elaine", "Courtney", "Esther", "Cindy", "Vanessa", "Brianna", "Lucas", "Norman", "Marvin", "Tracy", "Tony", "Monica", "Antonio", "Glenn", "Melanie"};

  static final String[] lastNames = {"Smith", "Johnson", "Williams", "Jones", "Brown", "Davis", "Miller", "Wilson", "Moore", "Taylor", "Anderson", "Thomas", "Jackson", "White", "Harris", "Martin", "Thompson", "GArcia", "Martinez", "Robinson", "Clark", "Rodriguez", "Lewis", "Lee", "Walker", "Hall", "Allen", "Young", "Hernandez", "King", "Wright", "Lopez", "Hill", "Scott", "Green", "Adams", "Baker", "Gonzalez", "Nelson", "Carter", "Mitchell", "Perez", "Roberts", "Turner", "Phillips", "Campbell", "Parker", "Evans", "Edwards", "Collins", "Stewart", "Sanchez", "Morris", "Rogers", "Reed", "Cook", "Morgan", "Bell", "Murphy", "Bailey", "Rivera", "Cooper", "Richardson", "Cox", "Howard", "Ward", "Torres", "Peterson", "Gray", "Ramirez", "James", "Watson", "Brooks", "Kelly", "Sanders", "Price", "Bennett", "Wood", "Barnes", "Ross", "Henderson", "Coleman", "Jenkins", "Perry", "Powell", "Long", "Patterson", "Hughes", "Flores", "Washington", "Butler", "Simmons", "Foster", "Gonzales", "Bryant", "Alexander", "Russell", "Griffin", "Diaz", "Hayes", "Myers", "Ford", "Hamilton", "Graham", "Sullivan", "Wallace", "Woods", "Cole", "West", "Jordan", "Owens", "Reynolds", "Fisher", "Ellis", "Harrison", "Gibson", "McDonald", "Cruz", "Marshall", "Ortiz", "Gomez", "Murray", "Freeman", "Wells", "Webb", "Simpson", "Stevens", "Tucker", "Porter", "Hunter", "Hicks", "Crawford", "Henry", "Boyd", "Mason", "Morales", "Kennedy", "Warren", "Dixon", "Ramos", "Reyes", "Burns", "Gordon", "Shaw", "Holmes", "Rice", "Robertson", "Hunt", "Black", "Daniels", "Palmer", "Mills", "Nichols", "Grant", "Knight", "Ferguson", "Rose", "Stone", "Hawkins", "Dunn", "Perkins", "Hudson", "Spencer", "Gardner", "Stephens", "Payne", "Pierce", "Berry", "Matthews", "Arnold", "Wagner", "Willis", "Ray", "Watkins", "Olson", "Carroll", "Duncan", "Snyder", "Hart", "Cunningham", "Bradley", "Lane", "Andrews", "Ruiz", "Harper", "Fox", "Riley", "Armstrong", "Carpenter", "Weaver", "Greene", "Lawrence", "Elliott", "Chavez", "Sims", "Austin", "Peters", "Kelley", "Franklin", "Lawson", "Fields", "Gutierrez", "Ryan", "Schmidt", "Carr", "Vasquez", "Castillo", "Wheeler", "Chapman", "Oliver", "Montgomery", "Richards", "Williamson", "Johnston", "Banks", "Meyer", "Bishop", "McCoy", "Howell", "Alvarez", "Morrison", "Hansen", "Fernandez", "Garza", "Harvey", "Little", "Burton", "Stanley", "Nguyen", "George", "Jacobs", "Reid", "Kim", "Fuller", "Lynch", "Dean", "Gilbert", "Garrett", "Romero", "Welch", "Larson", "Frazier", "Burke", "Hanson", "Day", "Mendoza", "Moreno", "Bowman", "Medina", "Fowler", "Brewer", "Hoffman", "Carlson", "Silva", "Pearson", "Holland", "Douglas", "Fleming", "Jensen", "Vargas", "Byrd", "Davidson", "Hopkins", "May", "Terry", "Herrera", "Wade", "Soto", "Walters", "Curtis", "Neal", "Caldwell", "Lowe", "Jennings", "Barnett", "Graves", "Jimenez", "Horton", "Shelton", "Barrett", "Obrien", "Castro", "Sutton", "Gregory", "McKinney", "Lucas", "Miles", "Craig", "Rodriquez", "Chambers", "Holt", "Lambert", "Fletcher", "Watts", "Bates", "Hale", "Rhodes", "Pena", "Beck", "Newman"
  };
}
