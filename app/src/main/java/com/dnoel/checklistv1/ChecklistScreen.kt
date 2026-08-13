package com.dnoel.checklistv1

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistScreen(listId: Int, listName: String, onBack: () -> Unit) {
    val viewModel: ChecklistViewModel = viewModel(
        key = "checklist_$listId",
        factory = ChecklistViewModel.factory(LocalContext.current.applicationContext, listId)
    )
    val dbItems by viewModel.items.collectAsState(initial = emptyList())
    val visibleDbItems = dbItems.filter { !it.isChecked }
    var localItems by remember { mutableStateOf(visibleDbItems) }
    LaunchedEffect(visibleDbItems) { localItems = visibleDbItems }

    var newItemText by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        localItems = localItems.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }

    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(listName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newItemText,
                    onValueChange = { newItemText = it },
                    label = { Text("New item") },
                    modifier = Modifier.weight(1f)
                )
                Button(
                    modifier = Modifier.padding(start = 8.dp),
                    onClick = {
                        if (newItemText.isNotBlank()) {
                            viewModel.addItem(newItemText.trim())
                            newItemText = ""
                        }
                    }
                ) {
                    Text("Add")
                }
            }

            if (localItems.isEmpty()) {
                Text(
                    text = "No items yet — add one",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            LazyColumn(
                state = lazyListState,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                items(localItems, key = { it.id }) { item ->
                    ReorderableItem(reorderableState, key = item.id) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.isChecked,
                                onCheckedChange = { checked -> viewModel.setChecked(item, checked) }
                            )
                            Text(item.text, modifier = Modifier.weight(1f))
                            IconButton(
                                modifier = Modifier.draggableHandle(
                                    onDragStopped = { viewModel.reorder(localItems) }
                                ),
                                onClick = {}
                            ) {
                                Icon(Icons.Filled.DragHandle, contentDescription = "Reorder")
                            }
                        }
                    }
                }
            }
        }
    }
}
