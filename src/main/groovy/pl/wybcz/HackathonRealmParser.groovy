package pl.wybcz

class HackathonRealmParser {

    Map<String, List<String>> convertToRealmMultimap(List projectToCode) {
        Map emptyMultimap = [:].withDefault { [] }
        return projectToCode.inject(emptyMultimap) { Map acc, entry ->
            String realm = entry.name.split('-').last()
            acc[realm] << entry.name
            return acc
        }
    }
}
