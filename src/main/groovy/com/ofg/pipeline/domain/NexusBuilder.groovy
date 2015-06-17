package com.ofg.pipeline.domain
import groovy.transform.CompileStatic

@CompileStatic
class NexusBuilder {

    String repoUrl
    String mavenUsername

    void repoUrl(String repoUrl) {
        this.repoUrl = repoUrl
    }

    void mavenUsername(String mavenUsername) {
        this.mavenUsername = mavenUsername
    }
}
