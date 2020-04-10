package cz.muni.fi.rpg.partyList

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import cz.muni.fi.rpg.R
import cz.muni.fi.rpg.model.Party
import cz.muni.fi.rpg.model.firestore.FirestorePartyRepository
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext

class AssemblePartyDialog(
    private val userId: String,
    private val onSuccessListener: (Party) -> Unit
) : DialogFragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext get() = Dispatchers.Main

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity();

        val inflater = activity.layoutInflater;

        return AlertDialog.Builder(activity)
            .setTitle(R.string.assembleParty_title)
            .setView(inflater.inflate(R.layout.dialog_asssemble_party, null))
            .setPositiveButton(R.string.assemblyParty_submit) { _, _ -> dialogSubmitted() }
            .create()
    }

    private fun dialogSubmitted() {
        val partyNameInput = requireDialog().findViewById<TextInputEditText>(R.id.partyName)

        val party = Party(UUID.randomUUID(), partyNameInput.text.toString(), userId);

        val context = requireContext()

        launch {
            withContext(Dispatchers.IO) {
                FirestorePartyRepository().save(party)
            }

            Toast.makeText(
                context,
                "Party ${partyNameInput.text.toString()} was created",
                Toast.LENGTH_LONG
            ).show()

            dismiss()
            onSuccessListener(party)
        }
    }
}