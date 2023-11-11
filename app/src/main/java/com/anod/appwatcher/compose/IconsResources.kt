package com.anod.appwatcher.compose

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shop2
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anod.appwatcher.R


@Composable
fun SortIcon() {
    Icon(imageVector = Icons.Default.Sort, contentDescription = stringResource(id = R.string.sort))
}

@Composable
fun AddIcon(contentDescription: String? = null) {
    Icon(imageVector = Icons.Default.Add, contentDescription = contentDescription ?: stringResource(id = R.string.menu_add))
}

@Composable
fun FilterIcon() {
    Icon(imageVector = Icons.Default.FlashOn, contentDescription = stringResource(id = R.string.filter))
}

@Composable
fun RadioIcon(isChecked: Boolean) {
    Icon(
        imageVector = if (isChecked) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
        contentDescription = null
    )
}

@Composable
fun MoreMenuIcon() {
    Icon(imageVector = Icons.Default.MoreVert, contentDescription = stringResource(id = R.string.menu))
}

@Composable
fun ExpandMenuIcon() {
    Icon(imageVector = Icons.Default.ArrowRight, contentDescription = null)
}

@Composable
fun SearchIcon() {
    Icon(
        imageVector = Icons.Default.Search,
        contentDescription = stringResource(id = R.string.menu_filter)
    )
}

@Composable
fun BackArrowIcon() {
    Icon(
        imageVector = Icons.Default.ArrowBack,
        contentDescription = stringResource(id = R.string.back)
    )
}

@Composable
fun EditIcon() {
    Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(id = R.string.menu_edit))
}

@Composable
fun PinShortcutIcon() {
    Icon(imageVector = Icons.Default.TouchApp, contentDescription = stringResource(id = R.string.pin_shortcut))
}

@Composable
fun PlayStoreAppIcon() {
    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = stringResource(id = R.string.open_play_store))
}

@Composable
fun ShareIcon() {
    Icon(imageVector = Icons.Default.Share, contentDescription = stringResource(id = R.string.share))
}

@Composable
fun InstalledSignIcon(modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Default.Smartphone,
        contentDescription = stringResource(id = R.string.installed),
        modifier = modifier
    )
}

@Composable
fun StoreVersionSignIcon(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    Icon(
        imageVector = Icons.Default.Shop2,
        contentDescription = null,
        modifier = modifier,
        tint = tint
    )
}

@Composable
fun WatchedIcon(unwatch: Boolean, modifier: Modifier = Modifier, contentDescription: String? = null) {
    Icon(
        modifier = modifier,
        imageVector = if (unwatch) Icons.Default.VisibilityOff else Icons.Default.Visibility,
        contentDescription = contentDescription ?: stringResource(id = if (unwatch) R.string.watched else R.string.menu_add)
    )
}

@Composable
fun TagIcon(
    modifier: Modifier = Modifier,
    outlined: Boolean = false,
    tint: Color = LocalContentColor.current,
    contentDescription: String? = null
) {
    Icon(
        modifier = modifier,
        imageVector = if (outlined) Icons.Outlined.Label else Icons.Default.Label,
        contentDescription = contentDescription ?: stringResource(id = R.string.tag),
        tint = tint
    )
}

@Composable
fun TranslateIcon() {
    Icon(imageVector = Icons.Default.Translate, contentDescription = stringResource(id = R.string.translate))
}

@Composable
fun OpenAppIcon() {
    Icon(imageVector = Icons.Default.OpenInNew, contentDescription = stringResource(id = R.string.open))
}

@Composable
fun UninstallIcon() {
    Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(id = R.string.uninstall))
}

@Composable
fun AppInfoIcon() {
    Icon(imageVector = Icons.Default.Info, contentDescription = stringResource(id = R.string.app_info))
}

@Composable
fun OpenDrawerIcon() {
    Icon(imageVector = Icons.Default.Menu, contentDescription = stringResource(id = R.string.menu))
}

@Composable
fun RefreshIcon() {
    Icon(imageVector = Icons.Default.Refresh, contentDescription = stringResource(id = R.string.menu_refresh))
}

@Composable
fun PlayStoreMyAppsIcon() {
    Icon(imageVector = Icons.Default.Store, contentDescription = stringResource(id = R.string.play_store_my_apps))
}

@Composable
fun ClearIcon() {
    Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(id = R.string.play_store_my_apps))
}

@Preview(widthDp = 200)
@Composable
fun IconsResourcesPreview() {
    AppTheme {
        Surface {
            val icons = listOf<@Composable () -> Unit>(
                { SortIcon() },
                { AddIcon() },
                { FilterIcon() },
                { RadioIcon(isChecked = true) },
                { RadioIcon(isChecked = false) },
                { MoreMenuIcon() },
                { ExpandMenuIcon() },
                { SearchIcon() },
                { BackArrowIcon() },
                { EditIcon() },
                { PlayStoreAppIcon() },
                { PlayStoreMyAppsIcon() },
                { ShareIcon() },
                { InstalledSignIcon() },
                { StoreVersionSignIcon() },
                { WatchedIcon(unwatch = true) },
                { WatchedIcon(unwatch = false) },
                { TagIcon() },
                { TagIcon(outlined = true) },
                { OpenAppIcon() },
                { UninstallIcon() },
                { AppInfoIcon() },
                { OpenDrawerIcon() },
                { RefreshIcon() },
                { ClearIcon() },
                { PinShortcutIcon() }
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 20.dp),
            ) {
                items(icons.size) { index ->
                    icons[index]()
                }
            }
        }
    }
}