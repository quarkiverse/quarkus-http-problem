package io.quarkiverse.httpproblem.postprocessing;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "io.quarkiverse.resteasy.problem.postprocessing", importOptions = ImportOption.DoNotIncludeTests.class)
public class PostProcessingArchitectureTest {

    @ArchTest
    ArchRule postProcessorsShouldNotDependOnJackson = classes()
            .should().onlyDependOnClassesThat().resideOutsideOfPackage("com.fasterxml.jackson.(**)")
            .because("post processors should be json-provider agnostic");

    @ArchTest
    ArchRule postProcessorsShouldNotDependOnJsonB = classes()
            .should().onlyDependOnClassesThat().resideOutsideOfPackage("jakarta.json.(**)")
            .because("post processors should be json-provider agnostic");

}
