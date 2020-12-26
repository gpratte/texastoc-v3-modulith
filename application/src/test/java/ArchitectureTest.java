import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class ArchitectureTest {

  /**
   * Make sure the non-player modules (game, notification, season and supply) do not
   * access any classes in the player.exception, player.repository and player.service
   * packages.
   */
  @Test
  public void playerModuleInterfaceAndModel() {
    JavaClasses importedClasses = new ClassFileImporter().importPackages("com.texastoc.module");

    noClasses().that().resideInAnyPackage("..game..", "..notification..", "..season..", "..supply..")
      .should().dependOnClassesThat().resideInAnyPackage("..player.exception..", "..player.repository..", "..player.service..")
      .check(importedClasses);
  }

}
