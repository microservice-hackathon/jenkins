package pl.wybcz

import spock.lang.Specification

class HackathonRealmParserSpec extends Specification {

    def "should convert a list of projects to a multimap"() {
        given:
            List<GitProject> projects = ['build-waw', 'publish-waw', 'build-lodz', 'publish-lodz'].collect { new GitProject(it, it) }
        when:
            Map<String, List<String>> realmMap = new HackathonRealmParser().convertToRealmMultimap(projects)
        then:
            realmMap == [waw: ['build-waw', 'publish-waw'], lodz: ['build-lodz', 'publish-lodz']]
    }

}
