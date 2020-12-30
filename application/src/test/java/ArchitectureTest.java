import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class ArchitectureTest {

  /**
   * Make sure the non-player modules (game, notification, season and supply) do not
   * access any classes in the following packages
   * <ul>
   *   <li>player.exception</li>
   *   <li>player.repository</li>
   *   <li>player.service</li>
   * </ul>
   */
  @Test
  public void playerModuleInterfaceAndModel() {
    JavaClasses importedClasses = new ClassFileImporter().importPackages("com.texastoc.module");
    noClasses().that().resideInAnyPackage("..game..", "..notification..", "..season..", "..settings..")
      .should().dependOnClassesThat().resideInAnyPackage("..player.exception..", "..player.repository..", "..player.service..")
      .check(importedClasses);
  }

  /**
   * Make sure the non-settings modules (player, game, notification and season) do not
   * access any classes in the following packages
   * <ul>
   *   <li>settings.repository</li>
   *   <li>settings.service</li>
   * </ul>
   */
  @Test
  public void settingsModuleInterfaceAndModel() {
    JavaClasses importedClasses = new ClassFileImporter().importPackages("com.texastoc.module");
    noClasses().that().resideInAnyPackage("..player..", "..game..", "..notification..", "..season..")
      .should().dependOnClassesThat().resideInAnyPackage("..settings.repository..", "..settings.service..")
      .check(importedClasses);
  }

  /**
   * Make sure the non-game modules (player, settings, notification and season) do not access any classes in the following packages
   * <ul>
   *   <li>game.calculator</li>
   *   <li>game.config</li>
   *   <li>game.connector</li>
   *   <li>game.exception</li>
   *   <li>game.repository</li>
   *   <li>game.request</li>
   *   <li>game.service</li>
   * </ul>
   */
  @Test
  public void gameModuleInterfaceAndModel() {
    JavaClasses importedClasses = new ClassFileImporter().importPackages("com.texastoc.module");
    noClasses().that().resideInAnyPackage("..player..", "..settings..", "..notification..", "..season..")
      .should().dependOnClassesThat().resideInAnyPackage("..game.config..", "..game.connector..", "..game.exception..", "..game.repository..", "..game.request..", "..game.service..")
      .check(importedClasses);
  }

}
