package com.lvijay.robotonous.speak.festival;

public sealed interface FestivalResponse
            permits ResponseError, ResponseOk, ResponseWave, ResponseLisp {
}
