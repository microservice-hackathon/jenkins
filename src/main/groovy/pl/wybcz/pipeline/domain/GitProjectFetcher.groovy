package pl.wybcz.pipeline.domain

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

@CompileStatic
class GitProjectFetcher {
    private final boolean testMode
    private final URL reposApi

    GitProjectFetcher(boolean testMode, URL reposApi) {
        this.testMode = testMode
        this.reposApi = reposApi
    }

    def fetchRepos() {
        if (testMode) {
            return new JsonSlurper().parseText(''' [{
                    "name": "client-service-waw",
                    "clone_url": "http://git.com/sth"
            }]''')
        }
        return new JsonSlurper().parse(reposApi.newReader())
    }
}
