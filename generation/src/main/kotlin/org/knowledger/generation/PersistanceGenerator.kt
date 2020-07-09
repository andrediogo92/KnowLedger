package org.knowledger.generation

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import org.knowledger.annotations.AutoStorable
import org.knowledger.generation.Options.KAPT_KOTLIN_GENERATED_OPTION_NAME
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Name
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class PersistanceGenerator : AbstractProcessor() {
    private lateinit var kaptKotlinGenerated: File

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        //Trigger NPE if option is broken
        kaptKotlinGenerated = File(
            processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]!!
        )
    }

    override fun getSupportedAnnotationTypes(): Set<String> =
        setOf("pt.um.lei.masb.agent.annotations.AutoStorable")

    override fun getSupportedOptions(): Set<String> =
        setOf(KAPT_KOTLIN_GENERATED_OPTION_NAME)

    override fun process(
        annotations: Set<TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        val elements = roundEnv.getElementsAnnotatedWith(
            AutoStorable::class.java
        )
        for (element in elements) {
            if (element.kind != ElementKind.CLASS) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Must be applied only to classes",
                    element
                )
            } else {
                val typeElement = element.toTypeElementOrNull() ?: continue
                processSingleClass(
                    roundEnv,
                    processingEnv.elementUtils.getPackageOf(typeElement),
                    typeElement.simpleName,
                    typeElement.enclosedElements
                )
            }
        }
        return true
    }

    private fun processSingleClass(
        roundEnv: RoundEnvironment,
        packageElement: PackageElement,
        simpleName: Name,
        enclosedElements: List<Element>
    ) {
        val implements = TypeSpec.classBuilder("${simpleName}StorageAdapter")//.addSuperinterface()

        FileSpec
            .builder(packageElement.simpleName.toString(), simpleName.toString())
            .build().writeTo(kaptKotlinGenerated)
    }

    private fun Element.toTypeElementOrNull(): TypeElement? {
        if (this !is TypeElement) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR,
                "Invalid element type, class expected",
                this
            )
            return null
        }

        return this
    }
}