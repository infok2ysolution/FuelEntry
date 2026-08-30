package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.PsoFuelRateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PsoRateDao {
    @Query("SELECT * FROM pso_fuel_rates")
    fun getAllRatesFlow(): Flow<List<PsoFuelRateEntity>>

    @Query("SELECT * FROM pso_fuel_rates WHERE fuelType = :fuelType LIMIT 1")
    suspend fun getRateByType(fuelType: String): PsoFuelRateEntity?

    @Query("SELECT * FROM pso_fuel_rates WHERE fuelType = :fuelType LIMIT 1")
    fun getRateByTypeFlow(fuelType: String): Flow<PsoFuelRateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(rate: PsoFuelRateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rates: List<PsoFuelRateEntity>)

    @Update
    suspend fun update(rate: PsoFuelRateEntity)

    @Query("UPDATE pso_fuel_rates SET addFactorPkr = :factor, effectiveRatePkr = baseRatePkr + :factor")
    suspend fun updateGlobalAddFactor(factor: Double)
}
