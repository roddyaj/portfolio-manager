import { useEffect, useState } from "react";
import OverlayTrigger from 'react-bootstrap/OverlayTrigger';
import Popover from 'react-bootstrap/Popover';
import 'bootstrap/dist/css/bootstrap.min.css';
import './App.css';

function App() {
	const [accounts, setAccounts] = useState([]);
	const [selectedAccount, setSelectedAccount] = useState(null);
	const [portfolio, setPortfolio] = useState(null);

	useEffect(() => {
		const requestAccounts = async () => {
			const response = await fetch("http://localhost:8090/accounts");
			const accounts = await response.json();
			return accounts;
		};
		requestAccounts().then((accounts) => {
			setAccounts(accounts);
			if (accounts.length > 0) {
				setSelectedAccount(accounts[0]);
			}
		});
	}, []);

	useEffect(() => {
		const requestPortfolio = async () => {
			const response = await fetch(`http://localhost:8090/portfolio?accountName=${selectedAccount}`);
			const accounts = await response.json();
			return accounts;
		};
		if (selectedAccount) {
			requestPortfolio().then(setPortfolio);
		}
	}, [selectedAccount]);

	const accountOptions = accounts.map((account) => {
		return (<option key={account} value={account}>{account}</option>);
	});

	return (
		<div>
			<div className="pm-block">
				<div style={{ display: "flex" }}>
					<div className="pm-main-header-item"></div>
					<div className="pm-main-header-item title" style={{ textAlign: "center" }}>
						<select name="accounts" id="accounts" onChange={(e) => setSelectedAccount(e.target.value)}>
							{accountOptions}
						</select>
					</div>
					<div className="pm-main-header-item" style={{ textAlign: "right" }}></div>
				</div>
			</div>
			{renderPortfolio(portfolio)}
		</div>
	);
}

function renderPortfolio(portfolio) {
	if (!portfolio) {
		return (<div>No portfolio available.</div>);
	}

	const sharePositions = portfolio.positions.filter(p => !p.symbol.includes(" ")).sort((a, b) => b.marketValue - a.marketValue);
	const positionRows = sharePositions.map((position, i) => {
		const low = position['52WeekLow'];
		const high = position['52WeekHigh'];
		const priceRangePct = 100 * (position.price - low) / (high - low);
		const popover = (
			<Popover id={`popover-position-${i}`} style={{ maxWidth: 400 }}>
				<Popover.Header as="h3">{position.description}</Popover.Header>
				<Popover.Body>
					<table>
						<tbody>
							<tr><td className="l">52 week range:</td><td className="l">{`${low} - ${high} (${priceRangePct.toFixed(1)}%)`}</td></tr>
							{position.dividendYield && <tr><td className="l">Dividend yield:</td><td className="l">{`${position.dividendYield}%`}</td></tr>}
							{position.peRatio && <tr><td className="l">P/E ratio:</td><td className="l">{position.peRatio}</td></tr>}
						</tbody>
					</table>
				</Popover.Body>
			</Popover>
		);

		return (
			<tr key={position.symbol}>
				<td className="l f">
					<OverlayTrigger trigger={['hover', 'focus']} placement="right" overlay={popover}>
						<a href={`https://finance.yahoo.com/quote/${position.symbol}`} target="_blank" style={position.peRatio < 0 ? { backgroundColor: "#FDD" } : {}}>{position.symbol}</a>
					</OverlayTrigger>
				</td>
				<td>{position.quantity}</td>
				<td>{position.price.toFixed(2)}</td>
				<td>{position.marketValue.toFixed(2)}</td>
				<td style={{ color: position.dayChangePct >= 0 ? "green" : "#C00" }}>{Math.abs(position.dayChangePct).toFixed(2) + "%"}</td>
				<td style={{ color: position.gainLossPct >= 0 ? "green" : "#C00" }}>{Math.abs(position.gainLossPct).toFixed(2) + "%"}</td>
				<td>{position.percentOfAccount.toFixed(2) + "%"}</td>
			</tr>
		);
	});

	return (
		<div className="pm-row">
			<div className="pm-column">
				<div className="pm-block">
					<div className="pm-heading">
						<div className="pm-title">Positions ({sharePositions.length})</div>
					</div>
					<table>
						<thead>
							<tr>
								<th className="l f">Ticker</th>
								<th>#</th>
								<th>Price</th>
								<th>Value</th>
								<th>Day</th>
								<th>G/L</th>
								<th>Actual</th>
							</tr>
						</thead>
						<tbody>
							{positionRows}
						</tbody>
					</table>
				</div>
			</div>
		</div>
	);
}

export default App;
