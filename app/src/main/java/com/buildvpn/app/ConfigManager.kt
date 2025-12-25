package com.buildvpn.app

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class ServerConfig(
    val id: String,
    val name: String,
    val type: String,
    val address: String,
    val port: Int,
    val config: String
)

object ConfigManager {
    
    private const val PREFS_NAME = "vpn_configs"
    private const val KEY_SERVERS = "servers"
    private const val KEY_ACTIVE = "active_server"
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun saveServer(context: Context, server: ServerConfig) {
        val servers = getServers(context).toMutableList()
        servers.removeAll { it.id == server.id }
        servers.add(server)
        
        val jsonArray = JSONArray()
        servers.forEach { s ->
            jsonArray.put(JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("type", s.type)
                put("address", s.address)
                put("port", s.port)
                put("config", s.config)
            })
        }
        
        getPrefs(context).edit().putString(KEY_SERVERS, jsonArray.toString()).apply()
    }
    
    fun getServers(context: Context): List<ServerConfig> {
        val json = getPrefs(context).getString(KEY_SERVERS, "[]") ?: "[]"
        val servers = mutableListOf<ServerConfig>()
        
        try {
            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                servers.add(ServerConfig(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    type = obj.getString("type"),
                    address = obj.getString("address"),
                    port = obj.getInt("port"),
                    config = obj.getString("config")
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return servers
    }
    
    fun deleteServer(context: Context, id: String) {
        val servers = getServers(context).filter { it.id != id }
        val jsonArray = JSONArray()
        servers.forEach { s ->
            jsonArray.put(JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("type", s.type)
                put("address", s.address)
                put("port", s.port)
                put("config", s.config)
            })
        }
        getPrefs(context).edit().putString(KEY_SERVERS, jsonArray.toString()).apply()
    }
    
    fun setActiveServer(context: Context, id: String) {
        getPrefs(context).edit().putString(KEY_ACTIVE, id).apply()
    }
    
    fun getActiveServer(context: Context): ServerConfig? {
        val activeId = getPrefs(context).getString(KEY_ACTIVE, null) ?: return null
        return getServers(context).find { it.id == activeId }
    }
}
