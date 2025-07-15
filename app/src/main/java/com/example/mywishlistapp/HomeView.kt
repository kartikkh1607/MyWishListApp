package com.example.mywishlistapp

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mywishlistapp.Data.Wish

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeView(navController: NavHostController, viewModel: WishViewModel) {

    val context = LocalContext.current
    val wishList = viewModel.getAllWishes.collectAsState(initial = emptyList())

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        topBar = {
            AppBarView("WishList") {
                Toast.makeText(context, "Top bar clicked", Toast.LENGTH_SHORT).show()
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(20.dp),
                backgroundColor = colorResource(R.color.black),
                contentColor = colorResource(R.color.white),
                onClick = {
                    navController.navigate(Screen.AddScreen.route + "/0")
                }
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(wishList.value, key = { it.id }) { wish ->

                val dismissState = rememberDismissState(
                    confirmStateChange = { dismissValue ->
                        if (dismissValue == DismissValue.DismissedToEnd || dismissValue == DismissValue.DismissedToStart) {
                            viewModel.deleteWish(wish)
                            true
                        } else false
                    }
                )

                SwipeToDismiss(
                    state = dismissState,
                    directions = setOf(
                        DismissDirection.EndToStart
                    ),
                    dismissThresholds = { FractionalThreshold(0.25f) },
                    background = {
                        val color by animateColorAsState(
                            if(dismissState.dismissDirection == DismissDirection.EndToStart) {
                                colorResource(R.color.red)
                            } else {
                                colorResource(R.color.transparent)
                            },
                            label = ""
                        )
                        val Alignment = Alignment.CenterStart
                        Box(modifier = Modifier.fillMaxSize().background(color).
                        padding(horizontal = 20.dp),
                            contentAlignment = Alignment)
                        {
                            Icon(Icons.Default.Delete, contentDescription = "Delete",
                                tint = colorResource(R.color.white)
                            )
                        }
                    },
                    dismissContent = {
                        WishItem(wish = wish) {
                            navController.navigate(Screen.AddScreen.route + "/${wish.id}")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun WishItem(wish: Wish, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 8.dp, end = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.white)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    )
    {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = wish.title, fontWeight = FontWeight.ExtraBold)
            Text(text = wish.description)
        }
    }
}
