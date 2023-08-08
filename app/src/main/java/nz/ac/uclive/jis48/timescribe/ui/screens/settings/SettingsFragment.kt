package nz.ac.uclive.jis48.timescribe.ui.screens.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import nz.ac.uclive.jis48.timescribe.R
import nz.ac.uclive.jis48.timescribe.ui.theme.TimeScribeTheme

class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                TimeScribeTheme {
                    SettingsScreen()
                }
            }
        }
    }

    @Composable
    fun SettingsScreen() {

    }
}