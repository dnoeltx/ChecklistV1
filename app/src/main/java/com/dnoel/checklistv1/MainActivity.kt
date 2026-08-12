package com.dnoel.checklistv1

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
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
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dnoel.checklistv1.ui.theme.ChecklistV1Theme
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChecklistV1Theme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    var selectedList by remember { mutableStateOf<TodoList?>(null) }
    val list = selectedList

    if (list == null) {
        ListsScreen(onOpenList = { selectedList = it })
    } else {
        ChecklistScreen(
            listId = list.id,
            listName = list.name,
            onBack = { selectedList = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(
    onOpenList: (TodoList) -> Unit,
    viewModel: ListsViewModel = viewModel()
) {
    val dbLists by viewModel.lists.collectAsState(initial = emptyList())
    var localLists by remember { mutableStateOf(dbLists) }
    LaunchedEffect(dbLists) { localLists = dbLists }

    var newListName by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        localLists = localLists.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("List Manager") }) }
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
                    value = newListName,
                    onValueChange = { newListName = it },
                    label = { Text("New list") },
                    modifier = Modifier.weight(1f)
                )
                Button(
                    modifier = Modifier.padding(start = 8.dp),
                    onClick = {
                        if (newListName.isNotBlank()) {
                            viewModel.addList(newListName.trim())
                            newListName = ""
                        }
                    }
                ) {
                    Text("Add")
                }
            }

            if (localLists.isEmpty()) {
                Text(
                    text = "Nothing here yet — create your first list",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            LazyColumn(
                state = lazyListState,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                items(localLists, key = { it.list.id }) { entry ->
                    ReorderableItem(reorderableState, key = entry.list.id) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${entry.list.name} (${entry.remainingCount})",
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onOpenList(entry.list) }
                                    .padding(vertical = 12.dp)
                            )
                            IconButton(onClick = { viewModel.deleteList(entry.list) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete list")
                            }
                            IconButton(
                                modifier = Modifier.draggableHandle(
                                    onDragStopped = { viewModel.reorder(localLists.map { it.list }) }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistScreen(listId: Int, listName: String, onBack: () -> Unit) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: ChecklistViewModel = viewModel(
        key = "checklist_$listId",
        factory = viewModelFactory {
            initializer { ChecklistViewModel(application, listId) }
        }
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
