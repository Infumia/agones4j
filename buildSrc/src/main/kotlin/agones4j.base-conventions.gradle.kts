plugins {
  id("net.kyori.indra.publishing")
  id("net.kyori.indra.publishing.sonatype")
}

indraSonatype {
  useAlternateSonatypeOSSHost("s01")
}

indra {
  github("infumia", "agones4j")
  mitLicense()

  configurePublications {
    pom {
      developers {
        developer {
          id = "portlek"
        }
      }
    }
  }
}
