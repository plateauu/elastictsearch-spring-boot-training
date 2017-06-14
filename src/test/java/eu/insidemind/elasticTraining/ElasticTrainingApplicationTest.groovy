package eu.insidemind.elasticTraining

import spock.lang.Specification

class ElasticTrainingApplicationTest extends Specification {

    def 'should init an app'() {
        when:
        ElasticTrainingApplication.main()

        then:
        noExceptionThrown()
    }
}
