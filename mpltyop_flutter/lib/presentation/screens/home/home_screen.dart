import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../../../core/constants.dart';
import '../../../core/di.dart';
import '../../../core/di.dart';
import '../../../core/di.dart';
import '../../../domain/entities/track.dart';
import '../../widgets/track_card.dart';
import '../../widgets/track_tile.dart';
import '../../widgets/loading_skeleton.dart';

class HomeScreen extends HookConsumerWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final sections = [
      _SectionConfig('Made for you', ['lofi beats', 'chill mix']),
      _SectionConfig('Focus', ['focus music', 'study beats']),
      _SectionConfig('Chill vibes', ['synthwave', 'jazz relax']),
      _SectionConfig('High energy', ['phonk', 'workout music']),
    ];

    return CustomScrollView(
      slivers: [
        SliverAppBar(
          title: const Text(AppConstants.appName),
          floating: true,
          snap: true,
          bottom: PreferredSize(
            preferredSize: const Size.fromHeight(56),
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: SearchAnchor(
                builder: (context, controller) => SearchBar(
                  controller: controller,
                  hintText: 'Search songs, artists...',
                  leading: const Icon(Icons.search),
                  onTap: () => controller.openView(),
                ),
                suggestionsBuilder: (context, controller) => [],
              ),
            ),
          ),
        ),
        SliverPadding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
          sliver: SliverList(
            delegate: SliverChildBuilderDelegate(
              (context, index) {
                if (index == 0) {
                  return _QuickAccessSection(libraryController: ref.read(libraryControllerProvider.notifier));
                }
                final section = sections[(index - 1) % sections.length];
                return _HomeSection(section: section, sectionIndex: index - 1);
              },
              childCount: sections.length + 1,
            ),
          ),
        ),
      ],
    );
  }
}

class _SectionConfig {
  final String title;
  final List<String> queries;
  const _SectionConfig(this.title, this.queries);
}

class _QuickAccessSection extends ConsumerWidget {
  final dynamic libraryController;
  const _QuickAccessSection({required this.libraryController});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text('Quick access', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
        const SizedBox(height: 12),
        SizedBox(
          height: 140,
          child: Consumer(
            builder: (context, ref, _) {
              final state = ref.watch(libraryControllerProvider);
              final items = [
                if (state.likedTrackIds.isNotEmpty)
                  _QuickAccessCard(
                    title: 'Liked Songs',
                    subtitle: '${state.likedTrackIds.length} songs',
                    icon: Icons.favorite,
                    gradient: const LinearGradient(colors: [Color(0xFF4B2C85), Color(0xFFA94FD8)]),
                    onTap: () {}, // TODO: navigate to liked
                  ),
                ...state.playlists.take(3).map((p) => _QuickAccessCard(
                  title: p.name,
                  subtitle: '${p.trackCount} songs',
                  icon: Icons.queue_music,
                  gradient: const LinearGradient(colors: [Color(0xFF1E3264), Color(0xFF537AA8)]),
                  onTap: () {}, // TODO: navigate to playlist
                )),
              ];
              if (items.isEmpty) {
                return const Center(
                  child: Text('Start listening to build your library'),
                );
              }
              return ListView.separated(
                scrollDirection: Axis.horizontal,
                itemCount: items.length,
                separatorBuilder: (_, __) => const SizedBox(width: 12),
                itemBuilder: (_, i) => items[i],
              );
            },
          ),
        ),
        const SizedBox(height: 24),
      ],
    );
  }
}

class _QuickAccessCard extends StatelessWidget {
  final String title;
  final String subtitle;
  final IconData icon;
  final Gradient gradient;
  final VoidCallback onTap;
  const _QuickAccessCard({
    required this.title,
    required this.subtitle,
    required this.icon,
    required this.gradient,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: 160,
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          gradient: gradient,
          borderRadius: BorderRadius.circular(12),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Icon(icon, color: Colors.white, size: 32),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
                Text(subtitle, style: const TextStyle(color: Colors.white70, fontSize: 12)),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _HomeSection extends ConsumerStatefulWidget {
  final _SectionConfig section;
  final int sectionIndex;
  const _HomeSection({required this.section, required this.sectionIndex});

  @override
  ConsumerState<_HomeSection> createState() => _HomeSectionState();
}

class _HomeSectionState extends ConsumerState<_HomeSection> {
  final _scrollController = ScrollController();

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(widget.section.title, style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
            TextButton(
              onPressed: () {}, // TODO: view all
              child: const Text('View all'),
            ),
          ],
        ),
        const SizedBox(height: 12),
        SizedBox(
          height: 180,
          child: ListView.separated(
            controller: _scrollController,
            scrollDirection: Axis.horizontal,
            itemCount: 6,
            separatorBuilder: (_, __) => const SizedBox(width: 12),
            itemBuilder: (context, i) => _SectionTrackCard(query: widget.section.queries[i % widget.section.queries.length]),
          ),
        ),
        const SizedBox(height: 24),
      ],
    );
  }
}

class _SectionTrackCard extends HookConsumerWidget {
  final String query;
  const _SectionTrackCard({required this.query});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final tracksAsync = useState<AsyncValue<List<Track>>>(const AsyncValue.loading());
    final searchTracks = ref.read(searchTracksProvider);

    void _load() async {
      tracksAsync.value = const AsyncValue.loading();
      final result = await searchTracks(query, limit: 1);
      tracksAsync.value = result.fold(
        (failure) => AsyncValue.error(failure, StackTrace.current),
        (tracks) => AsyncValue.data(tracks),
      );
    }

    useEffect(() {
      _load();
      return null;
    }, [query]);

    return tracksAsync.value.when(
      data: (tracks) => tracks.isNotEmpty 
        ? TrackCard(track: tracks.first, isSectionCard: true)
        : const SizedBox(width: 140, child: Center(child: Text('No results'))),
      loading: () => const _TrackCardSkeleton(),
      error: (_, __) => const SizedBox(width: 140, child: Center(child: Text('Error'))),
    );
  }
}

class _TrackCardSkeleton extends StatelessWidget {
  const _TrackCardSkeleton();

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: 140,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: 140,
            height: 140,
            decoration: BoxDecoration(
              color: Colors.grey[800],
              borderRadius: BorderRadius.circular(8),
            ),
          ),
          const SizedBox(height: 8),
          Container(height: 16, width: 100, color: Colors.grey[800]),
          const SizedBox(height: 4),
          Container(height: 12, width: 80, color: Colors.grey[700]),
        ],
      ),
    );
  }
}