package com.texastoc.service;

import com.texastoc.TestConstants;
import com.texastoc.module.quarterly.repository.QuarterlySeasonRepository;
import com.texastoc.module.season.repository.SeasonHistoryRepository;
import com.texastoc.module.season.repository.SeasonRepository;
import com.texastoc.module.season.service.SeasonService;
import org.junit.Before;
import org.junit.Ignore;
import org.springframework.boot.test.mock.mockito.MockBean;

@Ignore
public class SeasonServiceTest implements TestConstants {

  private SeasonService service;

  @MockBean
  private SeasonRepository seasonRepository;
  @MockBean
  private QuarterlySeasonRepository qSeasonRepository;
  @MockBean
  private SeasonHistoryRepository seasonHistoryRepository;

  @Before
  public void before() {
    service = new SeasonService(seasonRepository);
  }

//  @Ignore
//  @Test
//  public void testCreateSeason() {
//
//    // Arrange
//    LocalDate now = LocalDate.now();
//    LocalDate start = LocalDate.of(now.getYear(), Month.MAY, 1);
//
//    Mockito.when(seasonRepository.save((Season) notNull())).thenReturn(1);
//    Mockito.when(qSeasonRepository.save((QuarterlySeason) notNull())).thenReturn(1);
////    Mockito.when(configRepository.get()).thenReturn(TestConstants.getTocConfig());
//
//    // Act
//    Season actual = service.createSeason(start.getYear());
//
//    // Assert
//    TestUtils.assertCreatedSeason(start, actual);
//
//    // Season repository called once
//    Mockito.verify(seasonRepository, Mockito.times(1)).save(Mockito.any(Season.class));
//
//    // Season argument has same start time
//    ArgumentCaptor<Season> seasonArg = ArgumentCaptor.forClass(Season.class);
//    Mockito.verify(seasonRepository).save(seasonArg.capture());
//    Assert.assertEquals(start, seasonArg.getValue().getStart());
//
//    // Config repository called one times
////    Mockito.verify(configRepository, Mockito.times(1)).get();
//
//    // Quarterly season repository called four times
//    Mockito.verify(qSeasonRepository, Mockito.times(4)).save(Mockito.any(QuarterlySeason.class));
//
//  }
//
//  @Test
//  public void testGetSeason() {
//
//    // Arrange
//    Season expectedSeason = Season.builder()
//      // @formatter:off
//      .id(1)
//      .build();
//    // @formatter:on
//
//    List<QuarterlySeason> qSeasons = new ArrayList<>(4);
//    for (int i = 1; i <= 4; i++) {
//      // @formatter:off
//      QuarterlySeason qSeason = QuarterlySeason.builder()
//        .id(i)
//        .quarter(Quarter.fromInt(i))
//        .build();
//      // @formatter:on
//      qSeasons.add(qSeason);
//    }
//
//    List<Game> games = new LinkedList<>();
//    // @formatter:off
//    Game game = Game.builder()
//      .id(1)
//      .build();
//    // @formatter:on
//    games.add(game);
//    expectedSeason.setGames(games);
//
//    Mockito.when(seasonRepository.get(1))
//      .thenReturn(Season.builder()
//        .id(1)
//        .build());
//
//    Mockito.when(qSeasonRepository.getBySeasonId(1)).thenReturn(qSeasons);
//
//    Mockito.when(gameRepository.getBySeasonId(1)).thenReturn(games);
//
//    // Act
//    Season actualSeason = service.getSeason(1);
//
//    // Season repository called once
//    Mockito.verify(seasonRepository, Mockito.times(1)).get(1);
//
//    // QuarterlySeason repository called once
//    Mockito.verify(qSeasonRepository, Mockito.times(1)).getBySeasonId(1);
//
//    // Game repository called once
//    Mockito.verify(gameRepository, Mockito.times(1)).getBySeasonId(1);
//
//
//    // Assert
//    Assert.assertNotNull("season return from get should not be null ", actualSeason);
//    Assert.assertEquals(expectedSeason.getId(), actualSeason.getId());
//
//    Assert.assertNotNull("quarterly seasons should not be null ", actualSeason.getQuarterlySeasons());
//    Assert.assertEquals(4, actualSeason.getQuarterlySeasons().size());
//    for (int i = 1; i <= 4; i++) {
//      QuarterlySeason qSeason = actualSeason.getQuarterlySeasons().get(i - 1);
//      Assert.assertNotNull(qSeason);
//      Assert.assertTrue(qSeason.getId() > 0);
//    }
//
//    Assert.assertNotNull("season games should not be null ", actualSeason.getGames());
//    Assert.assertEquals(1, actualSeason.getGames().size());
//    Assert.assertTrue(actualSeason.getGames().get(0).getId() > 0);
//
//  }
//
//  // See TODO for caching in SeasonService
//  @Ignore
//  @Test
//  public void testCacheSeason() {
//    // If the cached season last calculated date is equal to the current
//    // season's last calcuated date then return the cached value
//    Season season1 = Season.builder()
//      .id(1)
//      .buyInCost(100)
//      .build();
//
//    Mockito.when(seasonRepository.get(1)).thenReturn(season1);
//
//    Season season = service.getSeason(1);
//
//    Mockito.verify(seasonRepository, Mockito.times(1)).get(1);
//    Mockito.verify(seasonRepository, Mockito.times(0)).getLastCalculated(1);
//
//    Assert.assertNull("last calculated should be null", season.getLastCalculated());
//    Assert.assertEquals("buyInCost should be 100", 100, season.getBuyInCost());
//
//    //
//    // The season should be cached
//    //
//    Mockito.reset(seasonRepository);
//    Mockito.when(seasonRepository.getLastCalculated(1)).thenReturn(null);
//
//    season = service.getSeason(1);
//
//    Mockito.verify(seasonRepository, Mockito.times(0)).get(1);
//    Mockito.verify(seasonRepository, Mockito.times(1)).getLastCalculated(1);
//
//    Assert.assertNull("last calculated should be null", season.getLastCalculated());
//    Assert.assertEquals("buyInCost should be 100", 100, season.getBuyInCost());
//
//    //
//    // Change the last calculated so that the cached value is not returned
//    //
//    LocalDateTime now = LocalDateTime.now();
//    Mockito.reset(seasonRepository);
//    Mockito.when(seasonRepository.getLastCalculated(1)).thenReturn(now);
//
//    Season season2 = Season.builder()
//      .id(1)
//      .buyInCost(200)
//      .lastCalculated(now)
//      .build();
//
//    Mockito.when(seasonRepository.get(1)).thenReturn(season2);
//
//    season = service.getSeason(1);
//
//    Mockito.verify(seasonRepository, Mockito.times(1)).get(1);
//    Mockito.verify(seasonRepository, Mockito.times(1)).getLastCalculated(1);
//
//    Assert.assertEquals("last calculated should match", now, season.getLastCalculated());
//    Assert.assertEquals("buyInCost should be 200", 200, season.getBuyInCost());
//
//    //
//    // The season should be cached
//    //
//    Mockito.reset(seasonRepository);
//    Mockito.when(seasonRepository.getLastCalculated(1)).thenReturn(now);
//
//    season = service.getSeason(1);
//
//    Mockito.verify(seasonRepository, Mockito.times(0)).get(1);
//    Mockito.verify(seasonRepository, Mockito.times(1)).getLastCalculated(1);
//
//    Assert.assertEquals("last calculated should match", now, season.getLastCalculated());
//    Assert.assertEquals("buyInCost should be 200", 200, season.getBuyInCost());
//
//    //
//    // Change the last calculated so that the cached value is not returned
//    //
//    LocalDateTime later = LocalDateTime.now().plusSeconds(2l);
//    Mockito.reset(seasonRepository);
//    Mockito.when(seasonRepository.getLastCalculated(1)).thenReturn(later);
//
//    Season season3 = Season.builder()
//      .id(1)
//      .buyInCost(300)
//      .lastCalculated(later)
//      .build();
//
//    Mockito.when(seasonRepository.get(1)).thenReturn(season3);
//
//    season = service.getSeason(1);
//
//    Mockito.verify(seasonRepository, Mockito.times(1)).get(1);
//    Mockito.verify(seasonRepository, Mockito.times(1)).getLastCalculated(1);
//
//    Assert.assertEquals("last calculated should match", later, season.getLastCalculated());
//    Assert.assertEquals("buyInCost should be 300", 300, season.getBuyInCost());
//
//
//  }
}
