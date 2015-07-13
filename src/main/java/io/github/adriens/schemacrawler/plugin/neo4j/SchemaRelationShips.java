/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.adriens.schemacrawler.plugin.neo4j;

import org.neo4j.graphdb.RelationshipType;

/**
 *
 * @author adriens
 */
public enum SchemaRelationShips implements RelationshipType {
    IS_COLUMN_OF_TABLE, BELONGS_TO_SCHEMA
}
