package pl.wybcz

class HackathonRealmParser {

    Map<String, List<String>> convertToRealmMultimap(List projectToCode) {
        return projectToCode.inject([:]) { acc, entry ->
            String realm = entry.name.split('-').last()
            if(acc[realm] == null) {
                acc[realm] = [entry.name]
            } else {
                acc[realm] << entry.name
            }
            return acc
        }
    }
}
