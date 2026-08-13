package com.dnoel.checklistv1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dnoel.checklistv1.ui.theme.ChecklistV1Theme

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
