package com.lvijay.robotonous.speak.festival;

sealed interface FestivalResponse
            permits ResponseError, ResponseOk, ResponseWave, ResponseLisp {
}

final class ResponseError implements FestivalResponse {}

final class ResponseOk implements FestivalResponse {}

final record ResponseLisp(String lispForm) implements FestivalResponse {}

final record ResponseWave(byte[] wavData) implements FestivalResponse {}
