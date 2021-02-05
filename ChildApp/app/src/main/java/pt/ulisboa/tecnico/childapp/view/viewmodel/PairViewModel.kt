package pt.ulisboa.tecnico.childapp.view.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import pt.ulisboa.tecnico.childapp.repository.PairingRepository

class PairViewModel : ViewModel() {

    fun registerGuardian(childId: String, guardianId: String): LiveData<Boolean> {
        return PairingRepository.registerGuardian(childId, guardianId)
    }

}