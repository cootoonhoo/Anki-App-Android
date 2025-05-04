package com.AnkiAppAndroid.data.repository

import com.AnkiAppAndroid.data.dao.UsuarioDao
import com.AnkiAppAndroid.data.model.Usuario
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    @OptIn(ExperimentalUuidApi::class)
    suspend fun getOrCreateUsuarioUuid(): String {
        val existingUuid = usuarioDao.getUuid()
        if (existingUuid != null) {
            return existingUuid
        }

        val novoUsuario = Usuario()
        usuarioDao.insertUsuario(novoUsuario)

        return novoUsuario.uuid
    }

    suspend fun getUsuarioUuid(): String? {
        return usuarioDao.getUuid()
    }
}