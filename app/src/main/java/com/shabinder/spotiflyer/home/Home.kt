package com.shabinder.spotiflyer.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.TabDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.InsertLink
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.viewinterop.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shabinder.spotiflyer.R
import com.shabinder.spotiflyer.ui.SpotiFlyerTypography
import com.shabinder.spotiflyer.ui.colorAccent
import com.shabinder.spotiflyer.ui.colorPrimary

@Composable
fun Home(modifier: Modifier = Modifier) {
    val viewModel: HomeViewModel = viewModel()

    Column(modifier = modifier) {

        AuthenticationBanner(viewModel,modifier)
        SearchBar(viewModel,modifier)

        val selectedCategory by viewModel.selectedCategory.collectAsState()

        HomeTabBar(
            selectedCategory,
            HomeCategory.values(),
            viewModel::selectCategory,
            modifier
        )

    }
}

@Composable
fun AuthenticationBanner(viewModel: HomeViewModel, modifier: Modifier) {
    val authenticationStatus by viewModel.isAuthenticating.collectAsState()

    if (authenticationStatus) {
        // TODO show a progress indicator or similar
    }
}

@Composable
fun HomeTabBar(
    selectedCategory: HomeCategory,
    categories: Array<HomeCategory>,
    selectCategory: (HomeCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndex =categories.indexOfFirst { it == selectedCategory }
    val indicator = @Composable { tabPositions: List<TabPosition> ->
        HomeCategoryTabIndicator(
            Modifier.tabIndicatorOffset(tabPositions[selectedIndex])
        )
    }

    TabRow(
        selectedTabIndex = selectedIndex,
        indicator = indicator,
        modifier = modifier
    ) {
        categories.forEachIndexed { index, category ->
            Tab(
                selected = index == selectedIndex,
                onClick = { selectCategory(category) },
                text = {
                    Text(
                        text = when (category) {
                            HomeCategory.About -> stringResource(R.string.home_about)
                            HomeCategory.History -> stringResource(R.string.home_history)
                        },
                        style = MaterialTheme.typography.body2
                    )
                }
            )
        }
    }
}

@Composable
fun SearchBar(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
){
    val link by viewModel.link.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(top = 16.dp,bottom = 16.dp)
    ){
        TextField(
            leadingIcon = {
                Icon(Icons.Rounded.InsertLink,tint = Color.LightGray)
            },
            label = {Text(text = "Paste Link Here...",color = Color.LightGray)},
            value = link,
            onValueChange = { viewModel.updateLink(it) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.padding(12.dp).fillMaxWidth()
                .border(
                    BorderStroke(2.dp, Brush.horizontalGradient(listOf(colorPrimary, colorAccent))),
                    RoundedCornerShape(30.dp)
                ),
            backgroundColor = Color.Black,
            textStyle = AmbientTextStyle.current.merge(TextStyle(fontSize = 20.sp)),
            shape = RoundedCornerShape(size = 30.dp),
            activeColor = Color.Transparent,
            inactiveColor = Color.Transparent
        )
        OutlinedButton(
            modifier = Modifier.padding(12.dp).wrapContentWidth(),
            onClick = {/*TODO*/},
            border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(colorPrimary, colorAccent)))
        ){
            Text(text = "Search",style = SpotiFlyerTypography.h6,modifier = Modifier.padding(4.dp))
        }
    }
}
@Composable
fun HomeCategoryTabIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.onSurface
) {
    Spacer(
        modifier.padding(horizontal = 24.dp)
            .preferredHeight(4.dp)
            .background(color, RoundedCornerShape(topLeftPercent = 100, topRightPercent = 100))
    )
}