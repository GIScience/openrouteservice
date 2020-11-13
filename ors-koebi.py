#!/usr/bin/python3

import openrouteservice as ors

from pprint import pprint


if __name__ == '__main__':
    coords = [[8.676581, 49.418204], [8.692803, 49.409465]]
    bbox = [[8.676581, 49.408204], [8.692803, 49.419465]]
    # host = "http://api.openrouteservice.org/"
    host = "http://localhost:8082/ors"
    profile = 'driving-car'
    formats = ['json', 'geojson']

    client = ors.Client(base_url=host, key='5b3ce3597851110001cf6248554c879acee241f6bc733402d2d351be')
    resp = client.request(
            url = f'/v2/centrality/{profile}/json',
            get_params = {},
            post_json = {
                'bbox': coords,
                })

    print(resp)

    #resp = client.request(
    #        url = f'/v2/matrix/{profile}/json',
    #        get_params = {},
    #        post_json = {
    #            "locations":[[8.67712,49.41699],[8.684177,49.413835],[8.687696,49.421122]],
    #            })

    print(resp)
    print(len(resp['centralityScores']))
    print(resp['locations'][0])
    exit()

    for format in formats:
        resp = client.request(
            url = f'/v2/directions/{profile}/{format}',
            get_params = {},
            post_json = {
                'coordinates': coords,
                'instructions': False
            })

        if format == 'json':
            print('JSON')
            print(resp['routes'][0])
            print()
        else:
            print('GeoJSON')
            print(resp)
