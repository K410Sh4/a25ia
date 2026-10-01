package dev.k410.a25ia.core

import dev.k410.a25ia.core.engine.IntentClassifier
import dev.k410.a25ia.core.model.UserIntent
import org.junit.Assert.assertEquals
import org.junit.Test

class IntentClassifierTest {
    @Test
    fun identityBeatsGreetingWhenBothExist() {
        assertEquals(
            UserIntent.IDENTITY,
            IntentClassifier.classify("oi quem é você?"),
        )
    }

    @Test
    fun memoryQuestionBeatsGenericQuestion() {
        assertEquals(
            UserIntent.MEMORY,
            IntentClassifier.classify("oi, qual é meu nome?"),
        )
    }
}
