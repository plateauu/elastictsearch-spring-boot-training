package eu.insidemind.elasticTraining.config

import spock.lang.Specification

class ElasticConfigTest extends Specification {

    def "should return true"(){
        expect:
        true
    }

    def "another test that should return true"(){
        def config = new ElasticConfig()
        expect:
        true
    }

}
