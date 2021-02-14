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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shabinder.common.di.giveDonation
import com.shabinder.common.models.DownloadRecord
import com.shabinder.common.main.SpotiFlyerMain.HomeCategory
import com.shabinder.common.di.openPlatform
import com.shabinder.common.di.shareApp
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
                Icon(Icons.Rounded.Edit,"Link Text Box",tint = Color.LightGray)
            },
            label = { Text(text = "Paste Link Here...",color = Color.LightGray) },
            singleLine = true,
            textStyle = TextStyle.Default.merge(TextStyle(fontSize = 18.sp,color = Color.White)),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = modifier.padding(12.dp).fillMaxWidth()
                .border(
                    BorderStroke(2.dp, Brush.horizontalGradient(listOf(colorPrimary, colorAccent))),
                    RoundedCornerShape(30.dp)
                ),
            backgroundColor = Color.Black,
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
            border = BorderStroke(1.dp,Color.Gray)
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
                        tint = Color.Unspecified,
                        modifier = Modifier.clickable(
                            onClick = { openPlatform("com.spotify.music","http://open.spotify.com") })
                    )
                    Spacer(modifier = modifier.padding(start = 16.dp))
                    Icon(imageVector = GaanaLogo(),
                        "Open Gaana",
                        tint = Color.Unspecified,
                        modifier = Modifier.clickable(
                            onClick = { openPlatform("com.gaana","http://gaana.com") })
                    )
                    Spacer(modifier = modifier.padding(start = 16.dp))
                    Icon(imageVector = YoutubeLogo(),
                        "Open Youtube",
                        tint = Color.Unspecified,
                        modifier = Modifier.clickable(
                            onClick = { openPlatform("com.google.android.youtube","http://m.youtube.com") })
                    )
                    Spacer(modifier = modifier.padding(start = 12.dp))
                    Icon(imageVector = YoutubeMusicLogo(),
                        "Open Youtube Music",
                        tint = Color.Unspecified,
                        modifier = Modifier.clickable(
                            onClick = { openPlatform("com.google.android.apps.youtube.music","https://music.youtube.com/") })
                    )
                }
            }
        }
        Spacer(modifier = Modifier.padding(top = 8.dp))
        Card(
            modifier = modifier.fillMaxWidth(),
            border = BorderStroke(1.dp,Color.Gray)//Gray
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
                    Icon(Icons.Rounded.Flag,"Help Translate",Modifier.size(32.dp))
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
                Row(
                    modifier = modifier.fillMaxWidth().padding(vertical = 6.dp)
                        .clickable(onClick = { giveDonation() }),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.MailOutline,"Support Developer")
                    Spacer(modifier = Modifier.padding(start = 16.dp))
                    Column {
                        Text(
                            text = "Donate",
                            style = SpotiFlyerTypography.h6
                        )
                        Text(
                            text = "If you think I deserve to get paid for my work, you can leave me some money here.",
                            style = SpotiFlyerTypography.subtitle2
                        )
                    }
                }
                Row(
                    modifier = modifier.fillMaxWidth().padding(vertical = 6.dp)
                        .clickable(onClick = {
                            shareApp()
                        }),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Share,"Share SpotiFlyer App")
                    Spacer(modifier = Modifier.padding(start = 16.dp))
                    Column {
                        Text(
                            text = "Share",
                            style = SpotiFlyerTypography.h6
                        )
                        Text(
                            text = "Share this app with your friends and family.",
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
    loadImage:suspend (String)-> ImageBitmap?,
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
    loadImage:suspend (String)-> ImageBitmap?,
    onItemClicked:(String)->Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically,modifier = Modifier.fillMaxWidth().padding(end = 8.dp)) {
        ImageLoad(
            { loadImage(item.coverUrl) },
            "Album Art",
            modifier = Modifier.height(75.dp).width(90.dp)
        )
        Column(modifier = Modifier.padding(horizontal = 8.dp).height(60.dp).weight(1f),verticalArrangement = Arrangement.SpaceEvenly) {
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
            imageVector = ShareImage(),
            "Research",
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
            .height(4.dp)
            .background(color, RoundedCornerShape(topStartPercent = 100, topEndPercent = 100))
    )
}