package pl.wybcz.pipeline.domain

class GitProjectFetcher {
    private final boolean testMode
    private final URL reposApi

    GitProjectFetcher(boolean testMode, URL reposApi) {
        this.testMode = testMode
        this.reposApi = reposApi
    }

    def fetchRepos() {
        if (testMode) {
            return new groovy.json.JsonSlurper().parseText(''' [{
                    "name": "client-service-waw",
                    "clone_url": "http://git.com/sth"
            }]''')
        }
        return new groovy.json.JsonSlurper().parse(reposApi.newReader())
    }
}
