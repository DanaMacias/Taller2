package com.example.taller2.data.model

sealed class JoinResult {
    object Success : JoinResult()
    object RoomNotFound : JoinResult()
    object RoomInactive : JoinResult()
    object RoomFull : JoinResult()
    object AlreadyJoined : JoinResult()
    object Error : JoinResult()
}
