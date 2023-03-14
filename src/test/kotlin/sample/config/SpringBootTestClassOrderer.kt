package sample.config

import org.junit.jupiter.api.ClassDescriptor
import org.junit.jupiter.api.ClassOrderer
import org.junit.jupiter.api.ClassOrdererContext
import sample.IntegrationTest
import java.util.Comparator

class SpringBootTestClassOrderer : ClassOrderer {

    override fun orderClasses(context: ClassOrdererContext) {
        context.classDescriptors.sortWith(Comparator.comparingInt(SpringBootTestClassOrderer::getOrder))
    }

    companion object {
        private fun getOrder(classDescriptor: ClassDescriptor): Int {
            if (classDescriptor.findAnnotation(IntegrationTest::class.java).isPresent) {
                return 2
            }
            return 1
        }
    }
}
