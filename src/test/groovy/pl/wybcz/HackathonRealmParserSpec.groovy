package pl.wybcz

import groovy.transform.EqualsAndHashCode
import spock.lang.Specification

class HackathonRealmParserSpec extends Specification {

    def "should convert a list of projects to a multimap"() {
        given:
            List<Entry> projects = ['build-waw', 'publish-waw', 'build-lodz', 'publish-lodz'].collect { new Entry(it) }
        when:
            Map<String, List<String>> realmMap = new HackathonRealmParser().convertToRealmMultimap(projects)
        then:
            realmMap == [waw: ['build-waw', 'publish-waw'], lodz: ['build-lodz', 'publish-lodz']]
    }

    @EqualsAndHashCode
    static class Entry {
        String name

        Entry(String name) {
            this.name = name
        }
    }
}
