version = "0.0"

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation(kotlin("stdlib", Versions.kotlin))
}

tasks {
    dokka {
        moduleName = "annotations"
        outputFormat = "html"
        outputDirectory = "$buildDir/javadoc"

        jdkVersion = 8

        // Specifies the location of the project source code on the Web.
        // If provided, Dokka generates "source" links for each declaration.
        // Repeat for multiple mappings
        linkMapping {
            // Unix based directory relative path to the root of the project
            // (where you execute gradle respectively).
            dir = "src/main/kotlin"

            // URL showing where the source code can be accessed through the
            // web browser.
            url = "https://github.com/Seriyin/KnowLedger/blob/master/annotations/src/main/kotlin"

            // Suffix which is used to append the line number to the URL.
            // Use #L for GitHub.
            suffix = "#L"
        }

    }
}