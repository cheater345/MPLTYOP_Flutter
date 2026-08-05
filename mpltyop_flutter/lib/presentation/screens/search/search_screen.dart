import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_hooks/flutter_hooks.dart';

import '../../../core/constants.dart';
import '../../providers/search_controller.dart';
import '../../../domain/entities/track.dart';
import '../../widgets/track_tile.dart';
import '../../widgets/loading_skeleton.dart';

class SearchScreen extends HookConsumerWidget {
  const SearchScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final searchController = useTextEditingController();
    final focusNode = useFocusNode();

    useEffect(() {
      Future.delayed(const Duration(milliseconds: 300), () => focusNode.requestFocus());
      return null;
    }, []);

    return Scaffold(
      appBar: AppBar(
        title: SearchAnchor(
          viewBuilder: (context, controller) {
            return SearchBar(
              controller: searchController,
              hintText: 'Search songs, artists...',
              leading: const Icon(Icons.search),
              onTap: () => controller.openView(),
              onChanged: (_) => ref.read(searchControllerProvider.notifier).search(searchController.text),
              onSubmitted: (value) => ref.read(searchControllerProvider.notifier).search(value),
            );
          },
          suggestionsBuilder: (context, controller) => [],
        ),
      ),
      body: Consumer(
        builder: (context, ref, _) {
          final searchState = ref.watch(searchControllerProvider);
          return searchState.when(
            data: (tracks) {
              if (tracks.isEmpty) {
                return const Center(child: Text('No results found'));
              }
              return ListView.builder(
                itemCount: tracks.length,
                itemBuilder: (context, index) => TrackTile(
                  track: tracks[index],
                  index: index + 1,
                  onTap: () => _playTrack(tracks[index], tracks, index, context),
                ),
              );
            },
            loading: () => ListView.builder(
              itemCount: 8,
              itemBuilder: (_, __) => const TrackTileSkeleton(),
            ),
            error: (error, _) => Center(child: Text(error.toString())),
          );
        },
      ),
    );
  }

  void _playTrack(Track track, List<Track> tracks, int index, BuildContext context) {
    // TODO: implement playback
  }
}