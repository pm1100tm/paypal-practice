package com.swd.shopswd.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Hexagonal Architecture 레이어 의존성 테스트.
 *
 * 의존성 방향: Adapter → Application → Domain
 * Domain은 어떤 레이어에도 의존하지 않음.
 */
@AnalyzeClasses(
    packages = "com.swd.shopswd",
    importOptions = ImportOption.DoNotIncludeTests.class
)
public class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule hexagonalLayersShouldBeRespected = layeredArchitecture()
        .consideringAllDependencies()

        // 레이어 정의
        .layer("Adapter").definedBy("..adapter..")
        .layer("Application").definedBy("..application..")
        .layer("Domain").definedBy("..domain..")

        // 의존성 규칙
        .whereLayer("Adapter").mayOnlyAccessLayers("Application", "Domain")
        .whereLayer("Application").mayOnlyAccessLayers("Domain")
        .whereLayer("Domain").mayNotAccessAnyLayer()

        // 외부 라이브러리/표준 라이브러리 의존성은 레이어 규칙에서 제외
        .ignoreDependency(
            JavaClass.Predicates.resideInAPackage("com.swd.shopswd.."),
            JavaClass.Predicates.resideOutsideOfPackage("com.swd.shopswd..")
        )

        .because("Hexagonal Architecture: 의존성은 항상 외부에서 내부로 향해야 함");

    @ArchTest
    static final ArchRule domainShouldBePureJava = noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat().resideInAnyPackage(
            "org.springframework..", "jakarta.persistence..", "tools.jackson..", "com.fasterxml.jackson..")
        .because("Domain 은 프레임워크에 의존하지 않는 순수 Java 여야 함");

    @ArchTest
    static final ArchRule inboundAdaptersShouldNotUseOutboundAdapters = noClasses()
        .that().resideInAPackage("..adapter.in..")
        .should().dependOnClassesThat().resideInAPackage("..adapter.out..")
        .because("Inbound Adapter 는 UseCase(Inbound Port) 를 통해서만 동작해야 함");
}
