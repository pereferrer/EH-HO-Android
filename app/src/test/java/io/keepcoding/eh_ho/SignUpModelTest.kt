package io.keepcoding.eh_ho

import io.keepcoding.eh_ho.data.SignUpModel
import org.junit.Test
import org.junit.Assert.assertEquals

class SignUpModelTest {

    @Test
    fun toJson_correct() {
        val model = SignUpModel(
            "test",
            "test@test.com",
            "password"
        )

        val json = model.toJson()

        assertEquals("test", json["name"])
        assertEquals("test", json["username"])
        assertEquals("test@test.com", json["email"])
        assertEquals("password", json["password"])
        assertEquals(true, json["active"])
        assertEquals(true, json["approved"])
    }
}