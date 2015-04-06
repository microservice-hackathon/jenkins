package pl.wybcz.pipeline.domain

import groovy.transform.CompileStatic
import groovy.transform.Immutable

@Immutable
@CompileStatic
class GitProject {
    String name
    String cloneUrl
}
