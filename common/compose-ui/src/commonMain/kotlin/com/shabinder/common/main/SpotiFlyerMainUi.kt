package com.shabinder.common.main

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.Icon
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shabinder.common.models.DownloadRecord
import com.shabinder.common.di.Picture
import com.shabinder.common.main.SpotiFlyerMain.HomeCategory
import com.shabinder.common.di.openPlatform
import com.shabinder.common.ui.*
import com.shabinder.common.ui.SpotiFlyerTypography

@Composable
fun SpotiFlyerMainContent(component: SpotiFlyerMain){
    val model by component.models.collectAsState(SpotiFlyerMain.State())

    Column {

        SearchPanel(
            model.link,
            component::onInputLinkChanged,
            component::onLinkSearch
        )

        HomeTabBar(
            model.selectedCategory,
            HomeCategory.values(),
            component::selectCategory
        )

        when(model.selectedCategory){
            HomeCategory.About -> AboutColumn()
            HomeCategory.History -> HistoryColumn(
                model.records,
                component::loadImage,
                component::onLinkSearch
            )
        }
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

    @Suppress("USELESS_CAST")//Showing Error in Latest Android Studio Canary
    TabRow(
        selectedTabIndex = selectedIndex,
        indicator = indicator as @Composable (List<TabPosition>) -> Unit,
        modifier = modifier,
    ) {
            categories.forEachIndexed { index, category ->
                Tab(
                    selected = index == selectedIndex,
                    onClick = { selectCategory(category) },
                    text = {
                        Text(
                            text = when (category) {
                                HomeCategory.About -> "About"
                                HomeCategory.History -> "History"
                            },
                            style = MaterialTheme.typography.body2
                        )
                    },
                    icon = {
                        when (category) {
                            HomeCategory.About -> Icon(Icons.Outlined.Info,"Info Tab")
                            HomeCategory.History -> Icon(Icons.Outlined.History,"History Tab")
                        }
                    }
                )
            }
        }
}

@Composable
fun SearchPanel(
    link:String,
    updateLink:(String) -> Unit,
    onSearch:(String) -> Unit,
    modifier: Modifier = Modifier
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(top = 16.dp)
    ){
        TextField(
            value = link,
            onValueChange = updateLink ,
            leadingIcon = {
                Icon(Icons.Rounded.Edit,"Link Text Box",tint = Color(0xFFCCCCCC))//LightGray
            },
            label = { Text(text = "Paste Link Here...",color = Color(0xFFCCCCCC)) },
            singleLine = true,
            //textStyle = LocalTextStyle.current.merge(TextStyle(fontSize = 18.sp,color = Color.White)),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = modifier.padding(12.dp).fillMaxWidth()
                .border(
                    BorderStroke(2.dp, Brush.horizontalGradient(listOf(colorPrimary, colorAccent))),
                    RoundedCornerShape(30.dp)
                ),
            backgroundColor = Color(0xFF000000),
            shape = RoundedCornerShape(size = 30.dp),
            activeColor = transparent,
            inactiveColor = transparent,
        )
        OutlinedButton(
            modifier = Modifier.padding(12.dp).wrapContentWidth(),
            onClick = {
                if(link.isBlank()) showPopUpMessage("Enter A Link!")
                else{
                    //TODO if(!isOnline(ctx)) showPopUpMessage("Check Your Internet Connection") else
                    onSearch(link)
                }
            },
            border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(colorPrimary, colorAccent)))
        ){
            Text(text = "Search",style = SpotiFlyerTypography.h6,modifier = Modifier.padding(4.dp))
        }
    }
}

@Composable
fun AboutColumn(modifier: Modifier = Modifier) {
    //TODO Make Scrollable
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Card(
            modifier = modifier.fillMaxWidth(),
            border = BorderStroke(1.dp,Color(0xFF888888))//Gray
        ) {
            Column(modifier.padding(12.dp)) {
                Text(
                    text = "Supported Platforms",
                    style = SpotiFlyerTypography.body1,
                    color = colorAccent
                )
                Spacer(modifier = Modifier.padding(top = 12.dp))
                Row(horizontalArrangement = Arrangement.Center,modifier = modifier.fillMaxWidth()) {
                    Icon(
                        imageVector = SpotifyLogo(),
                        "Open Spotify",
                        tint = unspecifiedColor,
                        modifier = Modifier.clickable(
                            onClick = { openPlatform("com.spotify.music","http://open.spotify.com") })
                    )
                    Spacer(modifier = modifier.padding(start = 16.dp))
                    Icon(imageVector = GaanaLogo(),
                        "Open Gaana",
                        tint = unspecifiedColor,
                        modifier = Modifier.clickable(
                            onClick = { openPlatform("com.gaana","http://gaana.com") })
                    )
                    Spacer(modifier = modifier.padding(start = 16.dp))
                    Icon(imageVector = YoutubeLogo(),
                        "Open Youtube",
                        tint = unspecifiedColor,
                        modifier = Modifier.clickable(
                            onClick = { openPlatform("com.google.android.youtube","http://m.youtube.com") })
                    )
                    Spacer(modifier = modifier.padding(start = 12.dp))
                    Icon(imageVector = YoutubeMusicLogo(),
                        "Open Youtube Music",
                        tint = unspecifiedColor,
                        modifier = Modifier.clickable(
                            onClick = { openPlatform("com.google.android.apps.youtube.music","https://music.youtube.com/") })
                    )
                }
            }
        }
        Spacer(modifier = Modifier.padding(top = 8.dp))
        Card(
            modifier = modifier.fillMaxWidth(),
            border = BorderStroke(1.dp,Color(0xFF888888))//Gray
        ) {
            Column(modifier.padding(12.dp)) {
                Text(
                    text = "Support Development",
                    style = SpotiFlyerTypography.body1,
                    color = colorAccent
                )
                Spacer(modifier = Modifier.padding(top = 6.dp))
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable(
                        onClick = { openPlatform("","http://github.com/Shabinder/SpotiFlyer") })
                        .padding(vertical = 6.dp)
                ) {
                    Icon(imageVector = GithubLogo(),"Open Project Repo",tint = Color(0xFFCCCCCC))
                    Spacer(modifier = Modifier.padding(start = 16.dp))
                    Column {
                        Text(
                            text = "GitHub",
                            style = SpotiFlyerTypography.h6
                        )
                        Text(
                            text = "Star / Fork the project on Github.",
                            style = SpotiFlyerTypography.subtitle2
                        )
                    }
                }
                Row(
                    modifier = modifier.fillMaxWidth().padding(vertical = 6.dp)
                        .clickable(onClick = { openPlatform("","http://github.com/Shabinder/SpotiFlyer") }),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Flag,"Help Translate",Modifier.preferredSize(32.dp))
                    Spacer(modifier = Modifier.padding(start = 16.dp))
                    Column {
                        Text(
                            text = "Translate",
                            style = SpotiFlyerTypography.h6
                        )
                        Text(
                            text = "Help us translate this app in your local language.",
                            style = SpotiFlyerTypography.subtitle2
                        )
                    }
                }
                /*Row(
                    modifier = modifier.fillMaxWidth().padding(vertical = 6.dp)
                        .clickable(onClick = { startPayment(mainActivity) }),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.MailOutline.copy(defaultHeight = 32.dp,defaultWidth = 32.dp))
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
                }*/
                /*Row(
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
                }*/
            }
        }
    }
}

@Composable
fun HistoryColumn(
    list: List<DownloadRecord>,
    loadImage:(String)-> Picture?,
    onItemClicked: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = {
            items(list) {
                DownloadRecordItem(
                    item = it,
                    loadImage,
                    onItemClicked
                )
            }
        },
        modifier = Modifier.padding(top = 8.dp).fillMaxSize()
    )
}

@Composable
fun DownloadRecordItem(
    item: DownloadRecord,
    loadImage:(String)-> Picture?,
    onItemClicked:(String)->Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically,modifier = Modifier.fillMaxWidth().padding(end = 8.dp)) {
        val pic = loadImage(item.coverUrl)
        ImageLoad(
            pic,
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
            imageVector = Icons.Rounded.Share,
            "Share App",
            modifier = Modifier.clickable(onClick = {
                //if(!isOnline(ctx)) showDialog("Check Your Internet Connection") else
                onItemClicked(item.link)
            })
        )
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
            .background(color, RoundedCornerShape(topStartPercent = 100, topEndPercent = 100))
    )
}