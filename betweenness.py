def betweenness_centrality(
    G, k=None, normalized=True, weight=None, endpoints=False, seed=None
):
    r"""Compute the shortest-path betweenness centrality for nodes.

    Betweenness centrality of a node $v$ is the sum of the
    fraction of all-pairs shortest paths that pass through $v$

    .. math::

       c_B(v) =\sum_{s,t \in V} \frac{\sigma(s, t|v)}{\sigma(s, t)}

    where $V$ is the set of nodes, $\sigma(s, t)$ is the number of
    shortest $(s, t)$-paths,  and $\sigma(s, t|v)$ is the number of
    those paths  passing through some  node $v$ other than $s, t$.
    If $s = t$, $\sigma(s, t) = 1$, and if $v \in {s, t}$,
    $\sigma(s, t|v) = 0$ [2]_.

    Parameters
    ----------
    G : graph
      A NetworkX graph.

    k : int, optional (default=None)
      If k is not None use k node samples to estimate betweenness.
      The value of k <= n where n is the number of nodes in the graph.
      Higher values give better approximation.

    normalized : bool, optional
      If True the betweenness values are normalized by `2/((n-1)(n-2))`
      for graphs, and `1/((n-1)(n-2))` for directed graphs where `n`
      is the number of nodes in G.

    weight : None or string, optional (default=None)
      If None, all edge weights are considered equal.
      Otherwise holds the name of the edge attribute used as weight.

    endpoints : bool, optional
      If True include the endpoints in the shortest path counts.

    seed : integer, random_state, or None (default)
        Indicator of random number generation state.
        See :ref:`Randomness<randomness>`.
        Note that this is only used if k is not None.

    Returns
    -------
    nodes : dictionary
       Dictionary of nodes with betweenness centrality as the value.

    See Also
    --------
    edge_betweenness_centrality
    load_centrality

    Notes
    -----
    The algorithm is from Ulrik Brandes [1]_.
    See [4]_ for the original first published version and [2]_ for details on
    algorithms for variations and related metrics.

    For approximate betweenness calculations set k=#samples to use
    k nodes ("pivots") to estimate the betweenness values. For an estimate
    of the number of pivots needed see [3]_.

    For weighted graphs the edge weights must be greater than zero.
    Zero edge weights can produce an infinite number of equal length
    paths between pairs of nodes.

    The total number of paths between source and target is counted
    differently for directed and undirected graphs. Directed paths
    are easy to count. Undirected paths are tricky: should a path
    from "u" to "v" count as 1 undirected path or as 2 directed paths?

    For betweenness_centrality we report the number of undirected
    paths when G is undirected.

    For betweenness_centrality_subset the reporting is different.
    If the source and target subsets are the same, then we want
    to count undirected paths. But if the source and target subsets
    differ -- for example, if sources is {0} and targets is {1},
    then we are only counting the paths in one direction. They are
    undirected paths but we are counting them in a directed way.
    To count them as undirected paths, each should count as half a path.

    References
    ----------
    .. [1] Ulrik Brandes:
       A Faster Algorithm for Betweenness Centrality.
       Journal of Mathematical Sociology 25(2):163-177, 2001.
       http://www.inf.uni-konstanz.de/algo/publications/b-fabc-01.pdf
    .. [2] Ulrik Brandes:
       On Variants of Shortest-Path Betweenness
       Centrality and their Generic Computation.
       Social Networks 30(2):136-145, 2008.
       http://www.inf.uni-konstanz.de/algo/publications/b-vspbc-08.pdf
    .. [3] Ulrik Brandes and Christian Pich:
       Centrality Estimation in Large Networks.
       International Journal of Bifurcation and Chaos 17(7):2303-2318, 2007.
       http://www.inf.uni-konstanz.de/algo/publications/bp-celn-06.pdf
    .. [4] Linton C. Freeman:
       A set of measures of centrality based on betweenness.
       Sociometry 40: 35–41, 1977
       http://moreno.ss.uci.edu/23.pdf
    """
    betweenness = dict.fromkeys(G, 0.0)  # b[v]=0 for v in G
    nodes = G

    for s in nodes:
        # use Dijkstra's algorithm
        S, P, sigma = _single_source_dijkstra_path_basic(G, s, weight)

        # accumulation
        betweenness = _accumulate_basic(betweenness, S, P, sigma, s)

    # rescaling
    betweenness = _rescale(
        betweenness,
        len(G),
        normalized=normalized,
        directed=G.is_directed(),
        k=k,
        endpoints=endpoints,
    )
    return betweenness

def _single_source_dijkstra_path_basic(G, s, weight):
    # modified from Eppstein
    S = []
    P = {}
    for v in G:
        P[v] = [] # P keeps track of predecessors on shortest paths.

    # Sigma contains the number of shortest paths from s to v
    sigma = dict.fromkeys(G, 0.0)  # sigma[v]=0 for v in G
    D = {} #D[v] contains the distance from s to v
    sigma[s] = 1.0 # sigma[v] contains the number of paths from s to v
    push = heappush
    pop = heappop
    seen = {s: 0}
    c = count()
    Q = []  # use Q as heap with (distance,node id) tuples 
    # Q is a heapq that returns the unvisited node with the known shortest
    # distance to s, and its predecessor.


    # we push into Q the tuple (distance to s, ??, predecessor on shortest path, node)
    push(Q, (0, next(c), s, s))
    while Q:

        # v is the node that we know the shortest path to.
        # It has length "dist" and its predecessor on the shortest path from s is "pred".
        (dist, _, pred, v) = pop(Q)
        if v in D:
            continue  # already searched this node.

        # since its predecessor is "pred", there are exactly sigma[pred] paths that lead to it
        sigma[v] += sigma[pred]  # count paths
        # Stack needed to "return vertices in order of non-increasing distance from s"
        S.append(v)

        # We keep track of distances and of the nodes that we have already visited.
        D[v] = dist

        # iterate over all nodes connected to v
        for w, edgedata in G[v].items():

            # distance from s to w via (v,w), i.e. shortest path from s that uses v.
            vw_dist = dist + edgedata.get(weight, 1)

            # if we have already visited node w (and thus know the shortest path to it)
            # we skip it.
            # otherwise, if the path from s to w via v is shorter than any path already found,             # we enqueue it.
            if w not in D and (w not in seen or vw_dist < seen[w]):
                seen[w] = vw_dist
                push(Q, (vw_dist, next(c), v, w))
                sigma[w] = 0.0
                P[w] = [v]

            # if the path from s to w via v is of equal length than a path already found,
            # going via v is an alternative. Thus, we add the number of paths
            # to v to the number of paths to w and we append v to the list of
            # predecessors on the shortest path from s to w.
            elif vw_dist == seen[w]:  # handle equal paths
                sigma[w] += sigma[v]
                P[w].append(v)
    return S, P, sigma

def _accumulate_basic(betweenness, S, P, sigma, s):
    delta = dict.fromkeys(S, 0)
    while S:
        w = S.pop()
        coeff = (1 + delta[w]) / sigma[w]
        for v in P[w]:
            delta[v] += sigma[v] * coeff
        if w != s:
            betweenness[w] += delta[w]
    return betweenness

def _rescale(betweenness, n, normalized, directed=False, k=None, endpoints=False):
    if normalized:
        if endpoints:
            if n < 2:
                scale = None  # no normalization
            else:
                # Scale factor should include endpoint nodes
                scale = 1 / (n * (n - 1))
        elif n <= 2:
            scale = None  # no normalization b=0 for all nodes
        else:
            scale = 1 / ((n - 1) * (n - 2))
    else:  # rescale by 2 for undirected graphs
        if not directed:
            scale = 0.5
        else:
            scale = None
    if scale is not None:
        if k is not None:
            scale = scale * n / k
        for v in betweenness:
            betweenness[v] *= scale
    return betweenness
