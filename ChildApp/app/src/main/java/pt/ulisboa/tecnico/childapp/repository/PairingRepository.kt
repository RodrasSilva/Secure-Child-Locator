package pt.ulisboa.tecnico.childapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pt.ulisboa.tecnico.childapp.repository.service.ServerApi

object PairingRepository {

    fun registerGuardian(childId: String, guardianId: String): LiveData<Boolean> {
        val ld = MutableLiveData<Boolean>()
        ServerApi.apply {
            executor.execute {
                ld.postValue(registerGuardian(childId, guardianId))
            }
        }
        return ld
    }
}