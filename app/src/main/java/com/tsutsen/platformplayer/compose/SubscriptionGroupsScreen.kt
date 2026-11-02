package com.tsutsen.platformplayer.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tsutsen.platformplayer.models.SubscriptionGroup
import com.tsutsen.platformplayer.states.StateSubscriptionGroups
import com.tsutsen.platformplayer.states.StateSubscriptions
import java.time.OffsetDateTime

/**
 * Subscription groups: make groups of channels (for example "News" or
 * "Gaming"), tick the channels that belong in each. Groups are stored in the
 * same format as the original app, so they also sync.
 */
@Composable
fun SubscriptionGroupsScreen(focusId: String? = null) {
    var version by remember { mutableIntStateOf(0) }
    var newName by remember { mutableStateOf("") }
    var openId by remember { mutableStateOf(focusId) }
    val groups = remember(version) { StateSubscriptionGroups.instance.getSubscriptionGroups().sortedBy { it.name.lowercase() } }
    val subs = remember { StateSubscriptions.instance.getSubscriptions().sortedBy { it.channel.name.lowercase() } }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { Text("Subscription groups", style = MaterialTheme.typography.headlineSmall) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New group") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                Button(
                    enabled = newName.isNotBlank(),
                    onClick = {
                        val g = SubscriptionGroup(newName.trim())
                        g.lastChange = OffsetDateTime.now()
                        StateSubscriptionGroups.instance.updateSubscriptionGroup(g)
                        openId = g.id
                        newName = ""
                        version++
                    },
                ) { Text("Add") }
            }
        }
        if (groups.isEmpty()) {
            item { Text("No groups yet.", style = MaterialTheme.typography.bodyMedium) }
        }
        items(groups, key = { it.id }) { group ->
            val open = openId == group.id
            Column(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth().clickable { openId = if (open) null else group.id }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(group.name, style = MaterialTheme.typography.titleMedium)
                        Text("${group.urls.size} channels", style = MaterialTheme.typography.bodySmall)
                    }
                    TextButton(onClick = {
                        StateSubscriptionGroups.instance.deleteSubscriptionGroup(group.id)
                        version++
                    }) { Text("Delete") }
                }
                if (open) {
                    subs.forEach { sub ->
                        val url = sub.channel.url
                        val checked = group.urls.contains(url)
                        Row(
                            Modifier.fillMaxWidth().clickable {
                                if (checked) group.urls.remove(url) else group.urls.add(url)
                                group.lastChange = OffsetDateTime.now()
                                StateSubscriptionGroups.instance.updateSubscriptionGroup(group)
                                version++
                            },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(checked = checked, onCheckedChange = null)
                            Text(sub.channel.name, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
                HorizontalDivider()
            }
        }
    }
}
