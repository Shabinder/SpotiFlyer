package com.shabinder.spotiflyer.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.TabDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.InsertLink
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.viewinterop.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
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

        val link by viewModel.link.collectAsState()
        val selectedCategory by viewModel.selectedCategory.collectAsState()

        AuthenticationBanner(viewModel,modifier)

        SearchBar(
            link,
            viewModel::updateLink,
            modifier
        )


        HomeTabBar(
            selectedCategory,
            HomeCategory.values(),
            viewModel::selectCategory,
            modifier
        )

        when(selectedCategory){
            HomeCategory.About -> AboutColumn(viewModel,modifier)
            HomeCategory.History -> HistoryColumn()
        }

    }
}


@Composable
fun AboutColumn(viewModel: HomeViewModel, modifier: Modifier) {
    Card(
        modifier = modifier.padding(8.dp).fillMaxWidth(),
        border = BorderStroke(1.dp,Color.Gray)
    ) {
        Column(modifier.padding(8.dp)) {
            Text(
                text = stringResource(R.string.supported_platform),
                style = SpotiFlyerTypography.body1
            )
            Spacer(modifier = Modifier.padding(top = 8.dp))
            Row(horizontalArrangement = Arrangement.Center,modifier = modifier.fillMaxWidth()) {
                Icon(imageVector = vectorResource(id = R.drawable.ic_spotify_logo ),tint = Color.Unspecified)
                Spacer(modifier = modifier.padding(start = 24.dp))
                Icon(imageVector = vectorResource(id = R.drawable.ic_gaana ),tint = Color.Unspecified)
                Spacer(modifier = modifier.padding(start = 24.dp))
                Icon(imageVector = vectorResource(id = R.drawable.ic_youtube),tint = Color.Unspecified)
            }
        }
    }
}

@Composable
fun HistoryColumn() {
    //TODO("Not yet implemented")
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
                },
                icon = {
                    when (category) {
                        HomeCategory.About -> Icon(Icons.Outlined.Info)
                        HomeCategory.History -> Icon(Icons.Outlined.History)
                    }
                }
            )
        }
    }
}

@Composable
fun SearchBar(
    link:String,
    updateLink:(s:String) -> Unit,
    modifier: Modifier = Modifier
){

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
            onValueChange = { updateLink(it) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.padding(12.dp).fillMaxWidth()
                .border(
                    BorderStroke(2.dp, Brush.horizontalGradient(listOf(colorPrimary, colorAccent))),
                    RoundedCornerShape(30.dp)
                ),
            backgroundColor = Color.Black,
            textStyle = AmbientTextStyle.current.merge(TextStyle(fontSize = 20.sp,color = Color.White)),
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