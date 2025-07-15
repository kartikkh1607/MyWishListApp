package com.example.mywishlistapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mywishlistapp.Data.Wish
import com.example.mywishlistapp.ui.theme.MyWishListAppTheme
import kotlinx.coroutines.launch

@Composable
fun AddEditDetailView(
    id: Long,
    viewModel: WishViewModel,
    navController: NavController
) {

    val snackMessage = remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    if(id != 0L){
        val wish = viewModel.getWishbyId(id).collectAsState(initial = Wish(0L , "" ,""))
        viewModel.wishTitleState = wish.value.title
        viewModel.wishDescriptionState = wish.value.description
    }else{
        viewModel.wishTitleState = ""
        viewModel.wishDescriptionState = ""
    }


    Scaffold(
        topBar = {
            AppBarView(
                title = if (id != 0L)
                    stringResource(R.string.update_wish)
                else
                    stringResource(R.string.add_wish)
            ) {
                navController.navigateUp()
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ){ paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            WishTextFields(
                label = "title",
                value = viewModel.wishTitleState,
                onValueChanged = {
                viewModel.onWishTitleChanged(it)
            })

            Spacer(modifier = Modifier.height(10.dp))

            WishTextFields(
                label = "description",
                value = viewModel.wishDescriptionState,
                onValueChanged = {
                    viewModel.onWishDescriptionChanged(it)
                })

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = {
                if(viewModel.wishTitleState.isNotEmpty() &&
                    viewModel.wishDescriptionState.isNotEmpty())
                {
                    if(id != 0L){
                        // Update Wish
                        viewModel.updateWish(
                            Wish(
                                id = id,
                                title = viewModel.wishTitleState.trim(),
                                description = viewModel.wishDescriptionState.trim()
                            )
                        )
                    }else{
                        // Add Wish
                        viewModel.addWish(
                            Wish(
                                title = viewModel.wishTitleState.trim(),
                                description = viewModel.wishDescriptionState.trim()
                            )
                        )
                        snackMessage.value = "Wish has been created"
                    }

                }else{
                    snackMessage.value = "Enter fields to create a wish"
                }
                scope.launch {
                    snackbarHostState.showSnackbar(snackMessage.value)
                    navController.navigateUp()

                }

            })
            {
                Text(
                    text = if (id != 0L) stringResource(id = R.string.update_wish)
                    else stringResource(R.string.add_wish),
                    style = TextStyle(
                        fontSize = 18.sp
                    )
                )


            }

        }
    }
}

@Composable
fun WishTextFields(
    label: String,
    value: String,
    onValueChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChanged,
        label = {
            Text(
                text = label,
                color = colorResource(R.color.black)
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colorResource(R.color.black),
            unfocusedBorderColor = colorResource(R.color.black),
            cursorColor = colorResource(R.color.black),
            focusedLabelColor = colorResource(R.color.black),
            unfocusedLabelColor = colorResource(R.color.black),
            focusedTextColor = colorResource(R.color.black),
            unfocusedTextColor = colorResource(R.color.black)
        )
    )
}


@Preview(showBackground = true)
@Composable
fun WishTextFieldsPreview() {
    MyWishListAppTheme {
        WishTextFields(
            label = "Text",
            value = "Text",
            onValueChanged = {}
        )
    }
}

