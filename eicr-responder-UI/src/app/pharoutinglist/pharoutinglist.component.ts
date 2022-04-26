import { Component, OnInit, OnDestroy, ViewChild, AfterViewInit } from '@angular/core';
import { PharoutingService } from '../pharouting.service';
import { Pharouting } from '../pharouting';
import { LiveAnnouncer } from '@angular/cdk/a11y';
import { MatSort, Sort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';

@Component({
  selector: 'app-pharoutinglist',
  templateUrl: './pharoutinglist.component.html',
  styleUrls: ['./pharoutinglist.component.css']
})
export class PharoutinglistComponent implements OnInit, AfterViewInit, OnDestroy {

  // dataSource: Array<Pharouting>;
  pharoutinglists: Array<Pharouting>;
  displayedColumns: string[] = ['phaAgencyCode', 'receiverProtocol', 'protocolType', 'endpointUrl'];
  dataSource: MatTableDataSource<Pharouting> = new MatTableDataSource<Pharouting>([]);

  constructor(private pharoutingService: PharoutingService, private _liveAnnouncer: LiveAnnouncer) { }

  @ViewChild(MatSort) sort: MatSort;

  ngAfterViewInit() {
    console.log("ngAfterViewInit ::::");
    this.dataSource.sort = this.sort;
  }

  ngOnInit(): void {
    this.pharoutingService.getPharoutingList().subscribe((data: Pharouting[]) => {
      this.pharoutinglists = data;
      // this.dataSource = data;
      this.dataSource = new MatTableDataSource(data);
      this.dataSource.sort = this.sort;
    })
  }


  /** Announce the change in sort state for assistive technology. */
  announceSortChange(sortState: Sort) {
    // This example uses English messages. If your application supports
    // multiple language, you would internationalize these strings.
    // Furthermore, you can customize the message to add additional
    // details about the values being sorted.
    if (sortState.direction) {
      this._liveAnnouncer.announce(`Sorted ${sortState.direction}ending`);
    } else {
      this._liveAnnouncer.announce('Sorting cleared');
    }
  }

  onRowClicked(row) {
    console.log('Row clicked: ', row);
  }

  ngOnDestroy() {
    this.pharoutinglists.forEach((element, index) => {
      this.pharoutinglists.splice(index, 1);
      console.log(this.pharoutinglists);
    });
  }
}
