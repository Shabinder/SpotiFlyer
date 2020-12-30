package com.shabinder.spotiflyer.ui.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.AmbientTextStyle
import androidx.compose.material.Icon
import androidx.compose.material.TabDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.viewinterop.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import com.shabinder.spotiflyer.MainActivity
import com.shabinder.spotiflyer.R
import com.shabinder.spotiflyer.navigation.navigateToPlatform
import com.shabinder.spotiflyer.ui.SpotiFlyerTypography
import com.shabinder.spotiflyer.ui.colorAccent
import com.shabinder.spotiflyer.ui.colorPrimary
import com.shabinder.spotiflyer.utils.mainActivity
import com.shabinder.spotiflyer.utils.openPlatform
import com.shabinder.spotiflyer.utils.sharedViewModel
import com.shabinder.spotiflyer.utils.showDialog

@Composable
fun Home(navController: NavController, modifier: Modifier = Modifier) {
    val viewModel: HomeViewModel = viewModel()

    Column(modifier = modifier) {

        val link by viewModel.link.collectAsState()
        val selectedCategory by viewModel.selectedCategory.collectAsState()

        AuthenticationBanner(viewModel,modifier)

        SearchPanel(
            link,
            viewModel::updateLink,
            navController,
            modifier
        )


        HomeTabBar(
            selectedCategory,
            HomeCategory.values(),
            viewModel::selectCategory,
            modifier
        )

        when(selectedCategory){
            HomeCategory.About -> AboutColumn()
            HomeCategory.History -> HistoryColumn()
        }

    }
}


@Composable
fun AboutColumn(modifier: Modifier = Modifier) {
    ScrollableColumn(modifier.fillMaxSize(),contentPadding = PaddingValues(16.dp)) {
        Card(
            modifier = modifier.fillMaxWidth(),
            border = BorderStroke(1.dp,Color.Gray)
        ) {
            Column(modifier.padding(12.dp)) {
                Text(
                    text = stringResource(R.string.supported_platform),
                    style = SpotiFlyerTypography.body1,
                    color = colorAccent
                )
                Spacer(modifier = Modifier.padding(top = 12.dp))
                Row(horizontalArrangement = Arrangement.Center,modifier = modifier.fillMaxWidth()) {
                    Icon(
                        imageVector = vectorResource(id = R.drawable.ic_spotify_logo), tint = Color.Unspecified,
                        modifier = Modifier.clickable(
                            onClick = { openPlatform("com.spotify.music","http://open.spotify.com") })
                    )
                    Spacer(modifier = modifier.padding(start = 24.dp))
                    Icon(imageVector = vectorResource(id = R.drawable.ic_gaana ),tint = Color.Unspecified,
                        modifier = Modifier.clickable(
                            onClick = { openPlatform("com.gaana","http://gaana.com") })
                    )
                    Spacer(modifier = modifier.padding(start = 24.dp))
                    Icon(imageVector = vectorResource(id = R.drawable.ic_youtube),tint = Color.Unspecified,
                        modifier = Modifier.clickable(
                            onClick = { openPlatform("com.google.android.youtube","http://m.youtube.com") })
                    )
                }
            }
        }
        Spacer(modifier = Modifier.padding(top = 8.dp))
        Card(
            modifier = modifier.fillMaxWidth(),
            border = BorderStroke(1.dp,Color.Gray)
        ) {
            Column(modifier.padding(12.dp)) {
                Text(
                    text = stringResource(R.string.support_development),
                    style = SpotiFlyerTypography.body1,
                    color = colorAccent
                )
                Spacer(modifier = Modifier.padding(top = 6.dp))
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable(
                        onClick = { openPlatform("http://github.com/Shabinder/SpotiFlyer") })
                        .padding(vertical = 6.dp)
                ) {
                    Icon(imageVector = vectorResource(id = R.drawable.ic_github ),tint = Color.LightGray)
                    Spacer(modifier = Modifier.padding(start = 16.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.github),
                            style = SpotiFlyerTypography.h6
                        )
                        Text(
                            text = stringResource(R.string.github_star),
                            style = SpotiFlyerTypography.subtitle2
                        )
                    }
                }
                Row(modifier = modifier.fillMaxWidth().padding(vertical = 6.dp),verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Flag.copy(defaultHeight = 32.dp,defaultWidth = 32.dp))
                    Spacer(modifier = Modifier.padding(start = 16.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.translate),
                            style = SpotiFlyerTypography.h6
                        )
                        Text(
                            text = stringResource(R.string.help_us_translate),
                            style = SpotiFlyerTypography.subtitle2
                        )
                    }
                }
                Row(modifier = modifier.fillMaxWidth().padding(vertical = 6.dp),verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.CardGiftcard.copy(defaultHeight = 32.dp,defaultWidth = 32.dp))
                    Spacer(modifier = Modifier.padding(start = 16.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.donate),
                            style = SpotiFlyerTypography.h6
                        )
                        Text(
                            text = stringResource(R.string.donate_subtitle),
                            style = SpotiFlyerTypography.subtitle2
                        )
                    }
                }
                Row(modifier = modifier.fillMaxWidth().padding(vertical = 6.dp),verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Share.copy(defaultHeight = 32.dp,defaultWidth = 32.dp))
                    Spacer(modifier = Modifier.padding(start = 16.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.share),
                            style = SpotiFlyerTypography.h6
                        )
                        Text(
                            text = stringResource(R.string.share_subtitle),
                            style = SpotiFlyerTypography.subtitle2
                        )
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.fillMaxWidth().padding(8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.made_with_love),
                color = colorPrimary,
                fontSize = 22.sp
            )
            Spacer(modifier = Modifier.padding(start = 4.dp))
            Icon(vectorResource(id = R.drawable.ic_heart),tint = Color.Unspecified)
            Spacer(modifier = Modifier.padding(start = 4.dp))
            Text(
                text = stringResource(id = R.string.in_india),
                color = colorPrimary,
                fontSize = 22.sp
            )
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
fun SearchPanel(
    link:String,
    updateLink:(s:String) -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier
){

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(top = 16.dp,)
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
            onClick = {navController.navigateToPlatform(link)},
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