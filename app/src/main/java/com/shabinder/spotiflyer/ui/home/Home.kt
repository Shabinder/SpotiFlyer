/*
 * Copyright (c)  2021  Shabinder Singh
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.spotiflyer.ui.home

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.InsertLink
import androidx.compose.material.icons.rounded.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.viewModel
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.razorpay.Checkout
import com.shabinder.spotiflyer.MainActivity
import com.shabinder.spotiflyer.R
import com.shabinder.spotiflyer.database.DownloadRecord
import com.shabinder.spotiflyer.navigation.navigateToTrackList
import com.shabinder.spotiflyer.ui.SpotiFlyerTypography
import com.shabinder.spotiflyer.ui.colorAccent
import com.shabinder.spotiflyer.ui.colorPrimary
import com.shabinder.spotiflyer.utils.isOnline
import com.shabinder.spotiflyer.utils.openPlatform
import com.shabinder.spotiflyer.utils.sharedViewModel
import com.shabinder.spotiflyer.utils.showDialog
import dev.chrisbanes.accompanist.coil.CoilImage
import org.json.JSONObject

@Composable
fun Home(
    navController: NavController,
    mainActivity: MainActivity,
    modifier: Modifier = Modifier) {
    val viewModel: HomeViewModel = viewModel()

    Column(modifier = modifier) {

        AuthenticationBanner(sharedViewModel.isAuthenticated,modifier)

        SearchPanel(
            sharedViewModel.link,
            sharedViewModel::updateLink,
            navController,
            modifier
        )

        HomeTabBar(
            viewModel.selectedCategory,
            HomeCategory.values(),
            viewModel::selectCategory,
            modifier
        )

        when(viewModel.selectedCategory){
            HomeCategory.About -> AboutColumn(mainActivity)
            HomeCategory.History -> HistoryColumn(viewModel.downloadRecordList,navController)
        }
    }
    //Update Download List
    viewModel.getDownloadRecordList()
    //reset Gradient
    sharedViewModel.resetGradient()
}


@Composable
fun AboutColumn(mainActivity: MainActivity,modifier: Modifier = Modifier) {
    val ctx = AmbientContext.current
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
                            onClick = { openPlatform("com.spotify.music","http://open.spotify.com",ctx) })
                    )
                    Spacer(modifier = modifier.padding(start = 24.dp))
                    Icon(imageVector = vectorResource(id = R.drawable.ic_gaana ),tint = Color.Unspecified,
                        modifier = Modifier.clickable(
                            onClick = { openPlatform("com.gaana","http://gaana.com",ctx) })
                    )
                    Spacer(modifier = modifier.padding(start = 24.dp))
                    Icon(imageVector = vectorResource(id = R.drawable.ic_youtube),tint = Color.Unspecified,
                        modifier = Modifier.clickable(
                            onClick = { openPlatform("com.google.android.youtube","http://m.youtube.com",ctx) })
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
                        onClick = { openPlatform("http://github.com/Shabinder/SpotiFlyer",ctx) })
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
                Row(
                    modifier = modifier.fillMaxWidth().padding(vertical = 6.dp)
                        .clickable(onClick = { openPlatform("http://github.com/Shabinder/SpotiFlyer", ctx) }),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                Row(
                    modifier = modifier.fillMaxWidth().padding(vertical = 6.dp)
                        .clickable(onClick = { startPayment(mainActivity) }),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                Row(
                    modifier = modifier.fillMaxWidth().padding(vertical = 6.dp)
                        .clickable(onClick = {
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Hey, checkout this excellent Music Downloader http://github.com/Shabinder/SpotiFlyer")
                                type = "text/plain"
                            }

                            val shareIntent = Intent.createChooser(sendIntent, null)
                            ctx.startActivity(shareIntent)
                        }),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
    }
}

@Composable
fun HistoryColumn(
    list: List<DownloadRecord>,
    navController: NavController
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = {
            items(list) {
                DownloadRecordItem(item = it,navController = navController)
            }
        },
        modifier = Modifier.padding(top = 8.dp).fillMaxSize()
    )
}

@Composable
fun DownloadRecordItem(item: DownloadRecord,navController: NavController) {
    val ctx = AmbientContext.current
    Row(verticalAlignment = Alignment.CenterVertically,modifier = Modifier.fillMaxWidth().padding(end = 8.dp)) {
        val imgUri = item.coverUrl.toUri().buildUpon().scheme("https").build()
        CoilImage(
            data = imgUri,
            //Loading Placeholder Makes Scrolling very stuttery
//            loading = { Image(vectorResource(id = R.drawable.ic_song_placeholder)) },
            error = {Image(vectorResource(id = R.drawable.ic_musicplaceholder))},
            contentScale = ContentScale.Inside,
//            fadeIn = true,
            modifier = Modifier.preferredHeight(75.dp).preferredWidth(90.dp)
        )
        Column(modifier = Modifier.padding(horizontal = 8.dp).preferredHeight(60.dp).weight(1f),verticalArrangement = Arrangement.SpaceEvenly) {
            Text(item.name,maxLines = 1,overflow = TextOverflow.Ellipsis,style = SpotiFlyerTypography.h6,color = colorAccent)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(horizontal = 8.dp).fillMaxSize()
            ){
                Text(item.type,fontSize = 13.sp)
                Text("Tracks: ${item.totalFiles}",fontSize = 13.sp)
            }
        }
        Image(
            imageVector = vectorResource(id = R.drawable.ic_share_open),
            modifier = Modifier.clickable(onClick = {
                if(!isOnline(ctx)) showDialog("Check Your Internet Connection")
                else navController.navigateToTrackList(item.link)
            })
        )
    }
}

private fun startPayment(mainActivity: MainActivity) {
    /*
    *  You need to pass current activity in order to let Razorpay create CheckoutActivity
    * */
    val co = Checkout().apply {
        setKeyID("rzp_live_3ZQeoFYOxjmXye")
        setImage(R.drawable.ic_launcher_foreground)
    }

    try {
        val preFill = JSONObject()

        val options = JSONObject().apply {
            put("name","SpotiFlyer")
            put("description","Thanks For the Donation!")
            //You can omit the image option to fetch the image from dashboard
            //put("image","https://github.com/Shabinder/SpotiFlyer/raw/master/app/SpotifyDownload.png")
            put("currency","INR")
            put("amount","4900")
            put("prefill",preFill)
        }

        co.open(mainActivity,options)
    }catch (e: Exception){
        showDialog("Error in payment: "+ e.message)
        e.printStackTrace()
    }
}


@Composable
fun AuthenticationBanner(isAuthenticated: Boolean, modifier: Modifier) {

    if (!isAuthenticated) {
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
        modifier = modifier,
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
    val ctx = AmbientContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(top = 16.dp)
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
            textStyle = AmbientTextStyle.current.merge(TextStyle(fontSize = 18.sp,color = Color.White)),
            shape = RoundedCornerShape(size = 30.dp),
            activeColor = Color.Transparent,
            inactiveColor = Color.Transparent
        )
        OutlinedButton(
            modifier = Modifier.padding(12.dp).wrapContentWidth(),
            onClick = {
                if(link.isBlank()) showDialog("Enter A Link!")
                else{
                    if(!isOnline(ctx)) showDialog("Check Your Internet Connection")
                    else navController.navigateToTrackList(link)
                }
            },
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