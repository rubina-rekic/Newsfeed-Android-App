package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun FilterSection(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedCategory == "Politika",
                onClick = { if (selectedCategory != "Politika") onCategorySelected("Politika") },
                label = { Text("Politika") },
                modifier = Modifier.padding(2.dp).testTag("filter_chip_pol")
            )

            FilterChip(
                selected = selectedCategory == "Sport",
                onClick = { if (selectedCategory != "Sport") onCategorySelected("Sport") },
                label = { Text("Sport") },
                modifier = Modifier.padding(2.dp).testTag("filter_chip_spo")
            )

            FilterChip(
                selected = selectedCategory == "Nauka/tehnologija",
                onClick = { if (selectedCategory != "Nauka/tehnologija") onCategorySelected("Nauka/tehnologija") },
                label = { Text("Nauka/Tehnologija") },
                modifier = Modifier.padding(2.dp).testTag("filter_chip_sci")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedCategory == "All",
                onClick = { if (selectedCategory != "All") onCategorySelected("All") },
                label = { Text("Sve") },
                modifier = Modifier.padding(2.dp).testTag("filter_chip_all")
            )

            FilterChip(
                selected = selectedCategory == "Zdravlje",
                onClick = { if (selectedCategory != "Zdravlje") onCategorySelected("Zdravlje") },
                label = { Text("Zdravlje") },
                modifier = Modifier.padding(2.dp)
            )
        }
    }
}


